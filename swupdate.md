# SWUpdate

* This document provides details on how to test the SWUpdate feature for apollon

Note: by default, SWUpdate feature is enabled

## Building and Storing the artifacts

* Build the image for apollon as described on the [QSG](https://code.siemens.com/IndustrialLinux/Base/website/-/blob/main/docs/apollon/quick-start-guide.md)
* After successful build, copy the image artifacts to a separate location:
```
$ ls build-temp/tmp/deploy/images/orin-nano/*.swu
build-temp/tmp/deploy/images/orin-nano/development-image-industrial-os-orin-nano-ebg.swu
build-temp/tmp/deploy/images/orin-nano/development-image-industrial-os-orin-nano.swu

$ mkdir swu-artifacts
$ cp build-temp/tmp/deploy/images/orin-nano/*.swu swu-artifacts/
```
* Rebuild the image using the steps from qsg
* Boot the device with the latest built development-image as we have stored artifacts of it in previous steps

## Swupdate Verification
### Scenario 1: SWUpdate: system_a => system_b

* Identify the partition from which the system booted
```
root@orin-nano:~# lsblk
NAME        MAJ:MIN RM   SIZE RO TYPE MOUNTPOINTS
loop0         7:0    0    16M  1 loop
zram0       252:0    0 635.1M  0 disk [SWAP]
zram1       252:1    0 635.1M  0 disk [SWAP]
zram2       252:2    0 635.1M  0 disk [SWAP]
zram3       252:3    0 635.1M  0 disk [SWAP]
zram4       252:4    0 635.1M  0 disk [SWAP]
zram5       252:5    0 635.1M  0 disk [SWAP]
nvme0n1     259:0    0 238.5G  0 disk
├─nvme0n1p1 259:1    0  20.8M  0 part
├─nvme0n1p2 259:2    0   512M  0 part
├─nvme0n1p3 259:3    0   512M  0 part
├─nvme0n1p4 259:4    0    20G  0 part /
├─nvme0n1p5 259:5    0    20G  0 part
├─nvme0n1p6 259:6    0     1G  0 part /config
└─nvme0n1p7 259:7    0 196.5G  0 part /data
```
**Note:** The lsblk command logs indicate that the system booted from partition /dev/nvme0n1p4 (system_a).

* Transfer the update-image file from swu-artifacts/development-image-industrial-os-orin-nano.swu via SSH or copy it via a separate USB.
* Check the bootloader ustate before SWUpdate:
```
root@orin-nano:~# bg_printenv

----------------------------
 Config Partition #0 Values:
in_progress:      no
revision:         2
kernel:           C:BOOT0:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:



----------------------------
 Config Partition #1 Values:
in_progress:      no
revision:         1
kernel:           C:BOOT1:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:
```
* Apply SWUpdate and reboot
```
root@orin-nano:~# swupdate -i development-image-industrial-os-orin-nano.swu
SWUpdate v

Licensed under GPLv2. See source distribution for detailed copyright notices.

[INFO ] : SWUPDATE started :  Software Update started !
[INFO ] : SWUPDATE running :  Installation in progress
[INFO ] : SWUPDATE successful ! SWUPDATE successful !
[INFO ] : No SWUPDATE running :  Waiting for requests...
root@orin-nano:~# reboot
```
* Check the bootloader ustate after SWUpdate:
```
root@orin-nano:~# bg_printenv

----------------------------
 Config Partition #0 Values:
in_progress:      no
revision:         2
kernel:           C:BOOT0:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:



----------------------------
 Config Partition #1 Values:
in_progress:      no
revision:         3
kernel:           C:BOOT1:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           2 (TESTING)

user variables:
```
**Note:** Verify Partition #1 ustate is 2 (TESTING) and revision incremented to 3.

* To confirm the successful completion of the SWUpdate, execute the command below and verify that the bootloader ustate is now 0
(OK).
```
root@orin-nano:~# bg_setenv -c
Environment update was successful.
root@orin-nano:~# bg_printenv

----------------------------
 Config Partition #0 Values:
in_progress:      no
revision:         2
kernel:           C:BOOT0:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:



----------------------------
 Config Partition #1 Values:
in_progress:      no
revision:         3
kernel:           C:BOOT1:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:
```
* Verify system is booted from system_b (/dev/nvme0n1p5) partition
```
root@orin-nano:~# lsblk -f
NAME        FSTYPE FSVER LABEL  UUID                                 FSAVAIL FSUSE% MOUNTPOINTS
loop0
zram0                                                                               [SWAP]
zram1                                                                               [SWAP]
zram2                                                                               [SWAP]
zram3                                                                               [SWAP]
zram4                                                                               [SWAP]
zram5                                                                               [SWAP]
nvme0n1
├─nvme0n1p1 vfat   FAT16 efi    4321-DCBA
├─nvme0n1p2 vfat   FAT32 BOOT0  4321-DCBB
├─nvme0n1p3 vfat   FAT32 BOOT1  4321-DCBC
├─nvme0n1p4 ext4   1.0   root   6765536b-e902-4a2f-bbeb-559b428dd724
├─nvme0n1p5 ext4   1.0          00000000-0000-0000-0000-000065e5e543   10.8G    40% /
├─nvme0n1p6 ext4   1.0   config e0cbc70f-c0c1-437a-abaf-c34ee5e3d950  906.2M     0% /config
└─nvme0n1p7 ext4   1.0   data   55ec00b7-a9d6-47ee-ac6e-bbb0211e30e4  196.1G     0% /data
```

### Scenario 2: SWUpdate: system_b => system_a

* Copy the latest .swu file from build directory development-image-industrial-os-orin-nano.swu to the apollon device via SSH or USB.
* Apply SWUpdate and reboot
```
root@orin-nano:~# swupdate -i development-image-industrial-os-orin-nano.swu
```
* After successful SWUpdate and reboot, check the bootloader ustate:
```
root@orin-nano:~# bg_printenv

----------------------------
 Config Partition #0 Values:
in_progress:      no
revision:         4
kernel:           C:BOOT0:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           2 (TESTING)

user variables:



----------------------------
 Config Partition #1 Values:
in_progress:      no
revision:         3
kernel:           C:BOOT1:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:
```
**Note:** Note: Verify Partition #0 ustate is 2 (TESTING) and revision incremented to 4.
* To confirm the successful completion of the SWUpdate, execute the command below and verify that the bootloader ustate is now 0 (OK).
```
root@orin-nano:~# bg_setenv -c
Environment update was successful.
root@orin-nano:~# bg_printenv

----------------------------
 Config Partition #0 Values:
in_progress:      no
revision:         4
kernel:           C:BOOT0:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:



----------------------------
 Config Partition #1 Values:
in_progress:      no
revision:         3
kernel:           C:BOOT1:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:
```
* Verify system is booted from system_a (/dev/nvme0n1p4) partition.
```
root@orin-nano:~# lsblk -f
NAME        FSTYPE FSVER LABEL  UUID                                 FSAVAIL FSUSE% MOUNTPOINTS
loop0
zram0                                                                               [SWAP]
zram1                                                                               [SWAP]
zram2                                                                               [SWAP]
zram3                                                                               [SWAP]
zram4                                                                               [SWAP]
zram5                                                                               [SWAP]
nvme0n1
├─nvme0n1p1 vfat   FAT16 efi    4321-DCBA
├─nvme0n1p2 vfat   FAT32 BOOT0  4321-DCBB
├─nvme0n1p3 vfat   FAT32 BOOT1  4321-DCBC
├─nvme0n1p4 ext4   1.0          00000000-0000-0000-0000-000065e5e543   10.8G    40% /
├─nvme0n1p5 ext4   1.0          00000000-0000-0000-0000-000065e5e543
├─nvme0n1p6 ext4   1.0   config e0cbc70f-c0c1-437a-abaf-c34ee5e3d950  906.2M     0% /config
└─nvme0n1p7 ext4   1.0   data   55ec00b7-a9d6-47ee-ac6e-bbb0211e30e4  196.1G     0% /data
```

### Scenario 3: SWUpdate: Rollback
* Copy the update-image.swu file from swu-artifacts/development-image-industrial-os-orin-nano.swu to device via SSH or USB
* Apply swupdate and reboot
**Note:** Currently system is booted from system_a (/dev/nvme0n1p4). Applying the SWUpdate will be applied to system_b (/dev/nvme0n1p5)
  ```
  root@orin-nano:~# swupdate -i development-image-industrial-os-orin-nano.swu
  ```
* Verify system is booted from system_b (/dev/nvme0n1p5) partition:
```
  root@orin-nano:~# bg_printenv

----------------------------
 Config Partition #0 Values:
in_progress:      no
revision:         4
kernel:           C:BOOT0:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:



----------------------------
 Config Partition #1 Values:
in_progress:      no
revision:         5
kernel:           C:BOOT1:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           2 (TESTING)

user variables:
root@orin-nano:~# lsblk -f
NAME        FSTYPE FSVER LABEL  UUID                                 FSAVAIL FSUSE% MOUNTPOINTS
loop0
zram0                                                                               [SWAP]
zram1                                                                               [SWAP]
zram2                                                                               [SWAP]
zram3                                                                               [SWAP]
zram4                                                                               [SWAP]
zram5                                                                               [SWAP]
nvme0n1
├─nvme0n1p1 vfat   FAT16 efi    4321-DCBA
├─nvme0n1p2 vfat   FAT32 BOOT0  4321-DCBB
├─nvme0n1p3 vfat   FAT32 BOOT1  4321-DCBC
├─nvme0n1p4 ext4   1.0          00000000-0000-0000-0000-000065e5e543
├─nvme0n1p5 ext4   1.0          00000000-0000-0000-0000-000065e5e543   10.8G    40% /
├─nvme0n1p6 ext4   1.0   config e0cbc70f-c0c1-437a-abaf-c34ee5e3d950  906.2M     0% /config
└─nvme0n1p7 ext4   1.0   data   55ec00b7-a9d6-47ee-ac6e-bbb0211e30e4  196.1G     0% /data
```
**Note:** Verify Partition #1 ustate is 2 (TESTING) and revision incremented.

*  To rollback the update, reboot the system without updating the bootloader environment:
```
root@orin-nano:~# reboot
```
* To verify the system rollback, confirm that the system has booted from /dev/nvme0n1p4 (system_a) and that the ustate of Partition #1 is 3 (FAILED):
```
root@orin-nano:~# bg_printenv

----------------------------
 Config Partition #0 Values:
in_progress:      no
revision:         4
kernel:           C:BOOT0:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           0 (OK)

user variables:



----------------------------
 Config Partition #1 Values:
in_progress:      no
revision:         0
kernel:           C:BOOT1:linux.efi
kernelargs:
watchdog timeout: 0 seconds
ustate:           3 (FAILED)

user variables:


root@orin-nano:~# lsblk -f
NAME        FSTYPE FSVER LABEL  UUID                                 FSAVAIL FSUSE% MOUNTPOINTS
loop0
zram0                                                                               [SWAP]
zram1                                                                               [SWAP]
zram2                                                                               [SWAP]
zram3                                                                               [SWAP]
zram4                                                                               [SWAP]
zram5                                                                               [SWAP]
nvme0n1
├─nvme0n1p1 vfat   FAT16 efi    4321-DCBA
├─nvme0n1p2 vfat   FAT32 BOOT0  4321-DCBB
├─nvme0n1p3 vfat   FAT32 BOOT1  4321-DCBC
├─nvme0n1p4 ext4   1.0          00000000-0000-0000-0000-000065e5e543    7.4G    57% /
├─nvme0n1p5 ext4   1.0          00000000-0000-0000-0000-000065e5e543
├─nvme0n1p6 ext4   1.0   config e0cbc70f-c0c1-437a-abaf-c34ee5e3d950  906.2M     0% /config
└─nvme0n1p7 ext4   1.0   data   55ec00b7-a9d6-47ee-ac6e-bbb0211e30e4  196.1G     0% /data
```

