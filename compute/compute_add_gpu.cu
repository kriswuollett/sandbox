/*
 * Copyright (c) 2012, Kristopher Wuollett
 * All rights reserved.
 *
 * This file is part of kriswuollett/compute.
 *
 * kriswuollett/compute is free software: you can redistribute it and/or modify
 * it under the terms of the BSD 3-Clause License as written in the COPYING
 * file.
 */
#ifndef _CUDA_ADD_GPU_CU_
#define _CUDA_ADD_GPU_CU_

#include <cuda_runtime.h>

__global__
void add_gpu_kernel(float * a, float * b, float * c, int len)
{ 
    int i = threadIdx.x + blockDim.x * blockIdx.x;
    if (i < len) c[i] = a[i] + b[i];
}

extern "C"
void add_gpu(dim3 &dimGrid, dim3 &dimBlock,
             float * a, float * b, float * c, int len);

void add_gpu(dim3 &dimGrid, dim3 &dimBlock,
             float * a, float * b, float * c, int len)
{
    add_gpu_kernel<<<dimGrid, dimBlock>>>(a, b, c, len);
}


#endif // define _CUDA_ADD_GPU_CU_
