#include "LibCL/ImageConvolution.cl"

__kernel void convolve(
                       read_only image2d_t inputImage,
                       write_only image2d_t outputImage,
                       __constant const float* matrix,
                       int ksize)
{
	convolveFloatImage(inputImage, matrix, ksize, outputImage);
}
