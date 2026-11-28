"""Verify Navigation3's Lifecycle Compose implementation is packaged in an APK."""

import struct
import sys
import zipfile

TARGET_DESCRIPTOR = b"Landroidx/lifecycle/compose/RememberLifecycleOwnerKt;"


def read_uleb128(data: bytes, offset: int) -> tuple[int, int]:
    value = 0
    shift = 0
    while True:
        if offset >= len(data):
            raise ValueError("Truncated DEX ULEB128 value")
        byte = data[offset]
        offset += 1
        value |= (byte & 0x7F) << shift
        if byte < 0x80:
            return value, offset
        shift += 7


def dex_defines_target(dex: bytes) -> bool:
    if len(dex) < 0x70 or dex[:3] != b"dex":
        return False

    try:
        string_count, string_offset = struct.unpack_from("<II", dex, 0x38)
        type_count, type_offset = struct.unpack_from("<II", dex, 0x40)
        class_count, class_offset = struct.unpack_from("<II", dex, 0x60)

        if class_offset + class_count * 32 > len(dex):
            return False
        if type_offset + type_count * 4 > len(dex):
            return False
        if string_offset + string_count * 4 > len(dex):
            return False

        for index in range(class_count):
            class_index = struct.unpack_from("<I", dex, class_offset + index * 32)[0]
            if class_index >= type_count:
                continue

            descriptor_index = struct.unpack_from(
                "<I", dex, type_offset + class_index * 4
            )[0]
            if descriptor_index >= string_count:
                continue

            string_data_offset = struct.unpack_from(
                "<I", dex, string_offset + descriptor_index * 4
            )[0]
            _, string_data_offset = read_uleb128(dex, string_data_offset)
            string_end = dex.find(bytes((0,)), string_data_offset)
            if string_end >= 0 and dex[string_data_offset:string_end] == TARGET_DESCRIPTOR:
                return True
    except (IndexError, struct.error, ValueError):
        return False

    return False


def verify_apk(apk_path: str) -> None:
    with zipfile.ZipFile(apk_path) as apk:
        dex_names = [
            name
            for name in apk.namelist()
            if name == "classes.dex"
            or (
                name.startswith("classes")
                and name.endswith(".dex")
                and name[7:-4].isdigit()
            )
        ]
        if any(dex_defines_target(apk.read(name)) for name in dex_names):
            print("Verified RememberLifecycleOwnerKt is defined in the APK DEX files.")
            return

    raise SystemExit(
        "APK does not define androidx.lifecycle.compose.RememberLifecycleOwnerKt; "
        "check that lifecycle-runtime-compose 2.10.0 is packaged."
    )


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit(f"Usage: {sys.argv[0]} <apk>")
    verify_apk(sys.argv[1])
