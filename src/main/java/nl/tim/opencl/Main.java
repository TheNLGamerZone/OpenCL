package nl.tim.opencl;

import org.jocl.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import static org.jocl.CL.*;

public class Main
{

    public static String getProgram(String source)
    {
        BufferedReader reader;
        StringBuilder builder = new StringBuilder();

        try
        {
            reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/" + source)));
            String line;

            while ((line = reader.readLine()) != null)
            {
                builder.append(line).append("\n");
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return builder.toString().trim();
    }

    /**
     * The entry point of this sample
     *
     * @param args Not used
     */
    public static void main(String args[])
    {
        // Create input- and output data
        int field[] = new int[] {3, 4, 1,
                                 2, 4, 1,
                                 1, 3, 7};

        int fieldRes[] = new int[9];

        System.out.println(Arrays.toString(field));

        Pointer fieldPointer = Pointer.to(field);
        Pointer sizePointer = Pointer.to(new int[]{2});
        Pointer resultPointer = Pointer.to(fieldRes);

        // ------- Begin boiler plate ---------
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_GPU;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        cl_context context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        cl_command_queue commandQueue =
                clCreateCommandQueue(context, device, 0, null);

        // --------- End boiler plate --------------

        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[2];
        memObjects[0] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * 9, fieldPointer, null);
        memObjects[1] = clCreateBuffer(context,
                CL_MEM_WRITE_ONLY,
                Sizeof.cl_int * 9, null, null);

        // Create the program from the source code
        cl_program program = clCreateProgramWithSource(context,
                1, new String[]{ getProgram("loopKernel.cu") }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "loopKernel", null);

        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0,
                Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1,
                Sizeof.cl_uint, Pointer.to(new int[]{2}));
        clSetKernelArg(kernel, 2,
                Sizeof.cl_mem, Pointer.to(memObjects[1]));

        // Set the work-item dimensions
        long global_work_size[] = new long[]{9};
        long local_work_size[] = new long[]{1};

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[1], CL_TRUE, 0,
                Sizeof.cl_int * 9, resultPointer, 0, null, null);

        /* Reusing field
        //TODO: Remove start
        Pointer newSrcA = Pointer.to(new float[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10});
        memObjects[0] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, newSrcA, null);
        clSetKernelArg(kernel, 0,
                Sizeof.cl_mem, Pointer.to(memObjects[0]));

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
                n * Sizeof.cl_float, dst, 0, null, null);
        //TODO: Remove end
        */

        // Release kernel, program, and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        // Print the result
        System.out.println("Result: " + java.util.Arrays.toString(fieldRes));
    }
}
