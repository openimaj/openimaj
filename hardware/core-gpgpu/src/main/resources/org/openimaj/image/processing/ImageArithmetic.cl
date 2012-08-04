kernel void subtractImage(read_only image2d_t src1, read_only image2d_t src2, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);

    float4 v1 = read_imagef(src1, smp, (int2) { x, y });
    float4 v2 = read_imagef(src2, smp, (int2) { x, y });

    write_imagef(dst, (int2) { x, y }, v1 - v2);
}

kernel void subtractConstant(read_only image2d_t src1, float4 amt, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float4 v1 = read_imagef(src1, smp, (int2) { x, y });
    float4 v2 = read_imagef(src2, smp, (int2) { x, y });
    
    write_imagef(dst, (int2) { x, y }, v1 - amt);
}

kernel void addImage(read_only image2d_t src1, read_only image2d_t src2, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float4 v1 = read_imagef(src1, smp, (int2) { x, y });
    float4 v2 = read_imagef(src2, smp, (int2) { x, y });
    
    write_imagef(dst, (int2) { x, y }, v1 + v2);
}

kernel void addConstant(read_only image2d_t src1, float4 amt, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float4 v1 = read_imagef(src1, smp, (int2) { x, y });
    float4 v2 = read_imagef(src2, smp, (int2) { x, y });
    
    write_imagef(dst, (int2) { x, y }, v1 + amt);
}

kernel void multiplyImage(read_only image2d_t src1, read_only image2d_t src2, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float4 v1 = read_imagef(src1, smp, (int2) { x, y });
    float4 v2 = read_imagef(src2, smp, (int2) { x, y });
    
    write_imagef(dst, (int2) { x, y }, v1 * v2);
}

kernel void multiplyConstant(read_only image2d_t src1, float4 amt, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float4 v1 = read_imagef(src1, smp, (int2) { x, y });
    float4 v2 = read_imagef(src2, smp, (int2) { x, y });
    
    write_imagef(dst, (int2) { x, y }, v1 * amt);
}

kernel void divideImage(read_only image2d_t src1, read_only image2d_t src2, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float4 v1 = read_imagef(src1, smp, (int2) { x, y });
    float4 v2 = read_imagef(src2, smp, (int2) { x, y });
    
    write_imagef(dst, (int2) { x, y }, v1 / v2);
}

kernel void divideConstant(read_only image2d_t src1, float4 amt, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);
    
    float4 v1 = read_imagef(src1, smp, (int2) { x, y });
    float4 v2 = read_imagef(src2, smp, (int2) { x, y });
    
    write_imagef(dst, (int2) { x, y }, v1 / amt);
}
