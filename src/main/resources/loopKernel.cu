__kernel void loopKernel(__global const int *field,
                         const uint size,
                         __global int *result)
{
    int gid = get_global_id(0);
    int sum = 0;

    for (int x = gid % 3; x < (gid % 3) + size; x++)
    {
        for (int y = gid / 3; y < (gid / 3) + size; y++)
        {
            if (x < 3 && y < 3)
            {
                sum += field[x + y * 3];
            }
        }
    }

    result[gid] = sum;
}