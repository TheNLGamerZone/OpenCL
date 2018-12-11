__kernel void sampleKernel(__global const int *field,
                           __global int *result)
{
    int gid = get_global_id(0);
    int x = gid % 10;
    int y = gid / 10;

    result[x + y * 10] = x + y;
}