#
# Copyright (c) 2012, Kristopher Wuollett
# All rights reserved.
#
# This file is part of kriswuollett/compute.
#
# kriswuollett/compute is free software: you can redistribute it and/or modify
# it under the terms of the BSD 3-Clause License as written in the COPYING
# file.
#

.PHONY: all build clean
.PRECIOUS: compute_%_gpu.o compute_%_cpu.o

CUDA_PATH	?= /usr/local/cuda-5.0
CUDA_INC_PATH	?= $(CUDA_PATH)/include
CUDA_BIN_PATH	?= $(CUDA_PATH)/bin
CUDA_LIB_PATH	?= $(CUDA_PATH)/lib64

NVCC		?= $(CUDA_BIN_PATH)/nvcc
GCC		?= g++
GCC2		?= g++

LDFLAGS		:= -L$(CUDA_LIB_PATH) -lcuda -lcudart
CCFLAGS		:= -m64
INCLUDES	:= -I$(CUDA_INC_PATH) -I.

NVCCFLAGS	:= -m64

EXTRA_NVCCFLAGS ?=
EXTRA_LDFLAGS   ?=

GENCODE_SM10    := -gencode arch=compute_10,code=sm_10
GENCODE_SM20    := -gencode arch=compute_20,code=sm_20
GENCODE_SM30    := -gencode arch=compute_30,code=sm_30 -gencode arch=compute_35,code=sm_35
GENCODE_FLAGS   := $(GENCODE_SM10) $(GENCODE_SM20) $(GENCODE_SM30)

APPS		:= compute_add

all: build

build: compute_add

clean:
	rm -f *.o $(APPS) 

compute_%_gpu.o: compute_%_gpu.cu
	$(NVCC) $(NVCCFLAGS) $(EXTRA_NVCCFLAGS) $(GENCODE_FLAGS) $(MAXRREGCOUNT) $(INCLUDES) -o $@ -c $<

compute_%_cpu.o: compute_%_cpu.cpp
	$(GCC) $(CCFLAGS) $(INCLUDES) -o $@ -c $<

compute_%: compute_%_cpu.o compute_%_gpu.o
	$(GCC) $(CCFLAGS) -o $@ $+ $(LDFLAGS) $(EXTRA_LDFLAGS)
