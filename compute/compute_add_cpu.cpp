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
#include <iostream>
#include <sstream>
#include <string>
#include <cstdlib>
#include <cstring>
#include <time.h>
#include <unistd.h>
#include <cuda_runtime.h>

#define STREAMSIZE 80
#define INITIALCOUNT 100
#define GPU_THREADS 1024

using namespace std;

static float* h_A = NULL;
static float* h_B = NULL;
static float* h_C = NULL;
static float* d_A = NULL;
static float* d_B = NULL;
static float* d_C = NULL;
static int cpu_capacity = 0;
static int gpu_capacity = 0;
static int elem_count = 0;
static bool verbose = false;

void add_cpu(float* h_A, float* h_B, float* h_C, int count)
{
	for ( ; count > 0; --count)
		*h_C++ = *h_A++ + *h_B++;
}

void print_vector(string name, float* v, int count)
{
	cerr << name << " = [" << endl;
	for (int i = 0; i < count; ++i)
		cout << "  " << v[i] << endl;
	cerr << "];" << endl;
}

void output_vector(float* v, int count)
{
	for (int i = 0; i < count; ++i) cout << v[i] << endl;
}

void usage()
{
	cout << "Usage: compute_add [-v] <cpu|gpu> FILE1 FILE2" << endl
		  << "or:    compute_add -" << endl
		  << "For each line of input in FILE1 and FILE2 sum the numbers" << endl
		  << "else use stdin and sum the two numbers separated by '\\t'." << endl;
}

void run_stdin(void (*init)(), void (*acc)(float&, float&), void (*done)())
{
    init();

    string line;
    while (getline(cin, line))
    {
        istringstream iss(line);

        float a, b;
        if (!(iss >> a >> b))
        {
            cerr << "Line did not contain two floats: " << line << endl;
            usage();
            exit(1);
        }

        acc(a, b);
    }

    if (verbose)
    {
        print_vector("A", h_A, elem_count);
        print_vector("B", h_B, elem_count);
    }

    done();

}


// ---------- add_simple -------------------------------------------------------

void add_simple_init_cpu()
{
    if (verbose)
        cerr << "add_simple_init_cpu()" << endl;

    cpu_capacity = INITIALCOUNT;
    elem_count = 0;

    h_A = (float*) malloc(sizeof(float) * cpu_capacity);
    h_B = (float*) malloc(sizeof(float) * cpu_capacity);
    h_C = (float*) malloc(sizeof(float) * cpu_capacity);
}

void add_simple_init_gpu()
{
    if (verbose)
        cerr << "add_simple_init_gpu()" << endl;

    add_simple_init_cpu();
    gpu_capacity = cpu_capacity;

    cudaMalloc((void**) &d_A, sizeof(float) * gpu_capacity);
    cudaMalloc((void**) &d_B, sizeof(float) * gpu_capacity);
    cudaMalloc((void**) &d_C, sizeof(float) * gpu_capacity);
}

inline void add_simple_acc_cpu(float& a, float& b)
{
    if (verbose)
        cerr << "add_simple_acc(" << a << ", " << b << "): capacity="
             << cpu_capacity << endl;

    if (elem_count == cpu_capacity)
    {
        cpu_capacity *= 2;
        if (verbose)
            cerr << "add_simple_acc resized capacity=" << cpu_capacity << endl;
        h_A = (float*) realloc(h_A, sizeof(float) * cpu_capacity);
        h_B = (float*) realloc(h_B, sizeof(float) * cpu_capacity);
        h_C = (float*) realloc(h_C, sizeof(float) * cpu_capacity);
    }

    h_A[elem_count] = a;
    h_B[elem_count] = b;
    elem_count++;
}

inline void add_simple_acc_gpu(float& a, float& b)
{
    if (verbose)
        cerr << "add_simple_gpu(" << a << ", " << b << "): capacity="
             << cpu_capacity << endl;

    add_simple_acc_cpu(a, b);
    
    if (gpu_capacity < cpu_capacity)
    {
        cudaFree(d_A);
        cudaFree(d_B);
        cudaFree(d_C);

        gpu_capacity = cpu_capacity;

        cudaMalloc((void**) &d_A, sizeof(float) * gpu_capacity);
        cudaMalloc((void**) &d_B, sizeof(float) * gpu_capacity);
        cudaMalloc((void**) &d_C, sizeof(float) * gpu_capacity);
    }
}

void add_simple_done_cpu()
{
    if (verbose)
        cerr << "add_simple_done_cpu()" << endl;

    float time;
    struct timespec start, stop;

    clock_gettime(CLOCK_REALTIME, &start);

    add_cpu(h_A, h_B, h_C, elem_count);

    clock_gettime(CLOCK_REALTIME, &stop);

    if (verbose)
        print_vector("C", h_C, elem_count);
    output_vector(h_C, elem_count);

    time = ((stop.tv_sec * 1000000000.0 + stop.tv_nsec)
         - (start.tv_sec * 1000000000.0 + start.tv_nsec))
         / 1000000.0;
    cerr << "cpu\t" << time << " ms" << endl;

    free(h_A); h_A = NULL;
    free(h_B); h_B = NULL;
    free(h_C); h_C = NULL;
}

extern "C" void add_gpu(dim3& dimGrid, dim3& dimBlock, float * a, float * b, float * c, int len);

void add_simple_done_gpu()
{
    if (verbose)
        cerr << "add_simple_done_gpu()" << endl;
    
    cudaEvent_t start, memCopied, calc, stop;
    float time;

    cudaEventCreate(&start);
    cudaEventCreate(&memCopied);
    cudaEventCreate(&calc);
    cudaEventCreate(&stop);

    cudaEventRecord(start, 0);
    cudaMemcpy(d_A, h_A, elem_count * sizeof(float), cudaMemcpyHostToDevice);
    cudaMemcpy(d_B, h_B, elem_count * sizeof(float), cudaMemcpyHostToDevice);
    cudaEventRecord(memCopied, 0);

    dim3 DimGrid((elem_count - 1) / GPU_THREADS + 1, 1, 1);
    dim3 DimBlock(GPU_THREADS, 1, 1);

    add_gpu(DimGrid, DimBlock, d_A, d_B, d_C, elem_count); 
    cudaEventRecord(calc, 0);

    cudaThreadSynchronize();

    cudaEventRecord(stop, 0);

    cudaMemcpy(h_C, d_C, elem_count * sizeof(float), cudaMemcpyDeviceToHost);

    cudaEventRecord(stop);

    if (verbose)
        print_vector("C", h_C, elem_count);
    output_vector(h_C, elem_count);

    cudaEventElapsedTime(&time, start, memCopied);
    cerr << "host2device\t" << time << " ms" << endl;
    cudaEventElapsedTime(&time, memCopied, calc);
    cerr << "gpu_kernel\t" << time << " ms" << endl;
    cudaEventElapsedTime(&time, calc, stop);
    cerr << "device2host\t" << time << " ms" << endl;

    cudaFree(d_A);
    cudaFree(d_B);
    cudaFree(d_C);

    free(h_A); h_A = NULL;
    free(h_B); h_B = NULL;
    free(h_C); h_C = NULL;
}


int main(int argc, char* const argv[])
{
	/*
	const int count = 10;
	
	float* h_A = (float*) malloc(count * sizeof(float));
	float* h_B = (float*) malloc(count * sizeof(float));
	float* h_C = (float*) malloc(count * sizeof(float));

	for (int i = 1; i <= count; ++i)
	{
		h_A[i] = (float) i;
		h_B[i] = (float) (i * 10);
	}

	print_vector("A", h_A, count);
	print_vector("B", h_B, count);

	add_cpu(h_A, h_B, h_C, count);

	print_vector("C", h_C, count);
	*/

    const string cpu("cpu");
    const string gpu("gpu");

    const int input_file_count = argc - optind;

    if (input_file_count != 2)
    {
        cerr << "Only input from stdin currently supported.  Only "
            << input_file_count << " arg was provided." << endl;
        usage();
        return 1;
    }

    if (strlen(argv[optind + 1]) != 1 || argv[optind + 1][0] != '-')
    {
        cerr << "Single argument was not '-'." << endl;
        usage();
        return 1;
    }

    /*
    for (int index = optind; index < argc; index++)
    {
        cout << "ARG: " << argv[index] << endl;
    }
    */

    if (cpu.compare(argv[optind]) == 0)
    {
        run_stdin(&add_simple_init_cpu, &add_simple_acc_cpu,
                &add_simple_done_cpu);
    }
    else if (gpu.compare(argv[optind]) == 0)
    {
        run_stdin(&add_simple_init_gpu, &add_simple_acc_gpu,
                &add_simple_done_gpu);
    }
    else 
    {
        cerr << argv[optind] << " compute type  not recognized." << endl;
        usage();
        return 1;
    }

	return 0;
}
