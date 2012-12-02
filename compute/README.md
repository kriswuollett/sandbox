Building
========

This software was originally written for x86_86 RHEL 6.X type systems.
Run `make`.  If that doesn't work, then you will have to install `make` and/or
modify the Makefile to compile the software.

Example Output
==============

[kris@cloud compute]$ time ./compute_add_test 1000000 cpu > /dev/null
cpu     13.6998 ms

real    0m6.578s
user    0m6.622s
sys     0m0.532s
[kris@cloud compute]$ time ./compute_add_test 1000000 gpu > /dev/null
host2device     3.72214 ms
gpu_kernel      0.100768 ms
device2host     4.616 ms

real    0m9.329s
user    0m8.916s
sys     0m1.000s

License
=======

Copyright (c) 2012, Kristopher Wuollett

All rights reserved.

kriswuollett/compute is free software: you can redistribute it and/or modify
it under the terms of the BSD 3-Clause License as written in the [COPYING]
file.

References
==========

[COPYING]: COPYING
