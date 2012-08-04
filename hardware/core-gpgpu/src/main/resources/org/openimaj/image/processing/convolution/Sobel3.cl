kernel void sobel3(read_only image2d_t src, write_only image2d_t dst) {
    const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    
    int x = get_global_id(0);
    int y = get_global_id(1);

    float gradientX
    float gradientY;
    float v;
    
    v = read_imagef(src, smp, (int2) { x-1, y-1 }).s0;
    gradientX = -v;
    gradientY = -v;
    
    v = read_imagef(src, smp, (int2) { x, y-1 }).s0;
    gradientY -= 2*v;
    
    v = read_imagef(src, smp, (int2) { x+1, y-1 }).s0;
    gradientY -= v;
    gradientX += v;
    
    v = read_imagef(src, smp, (int2) { x-1, y }).s0;
    gradientX -= 2*v;
    
    v = read_imagef(src, smp, (int2) { x+1, y }).s0;
    gradientX += 2*v;
    
    v = read_imagef(src, smp, (int2) { x-1, y+1 }).s0;
    gradientX -= v;
    gradientY += v;
    
    v = read_imagef(src, smp, (int2) { x, y+1 }).s0;
    gradientY += 2*v;
    
    v = read_imagef(src, smp, (int2) { x+1, y+1 }).s0;
    gradientY += v;
    gradientX += v;
    
    float G = sqrt(gradientX*gradientX+gradientY*gradientY);
    float A = atan2(gradientY, gradientX);
    
    write_imagef(dst, (int2) { x, y }, (float4) { G, A, gradientX, gradientY });
}