
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.jcraft.jsch.*;

public class awsTest {

    static AmazonEC2      ec2;

    private static void init() throws Exception {

        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (C:\\Users\\82109\\.aws\\credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("ap-northeast-2")	/* check the region at AWS console */
                .build();
    }

    public static void main(String[] args) throws Exception {
        init();

        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        int number = 0;

        while(true) {

            System.out.println("  1. list instance                2. available zones        ");
            System.out.println("  3. start instance               4. available regions      ");
            System.out.println("  5. stop instance                6. create instance        ");
            System.out.println("  7. reboot instance              8. list images            ");
            System.out.println("  9. condor_status                10. terminate instance    ");
            System.out.println("  11. describe security groups    12. snapshot ID           ");
            System.out.println("  13. list volumes                14. list VPCs             ");
            System.out.println("  15. list subnets by VPC         99. quit                  ");
            System.out.println("------------------------------------------------------------");


            System.out.print("Enter an integer: ");
            if(menu.hasNextInt()){
                number = menu.nextInt();
            } else {
                System.out.println("concentration!");
                break;
            }

            String instance_id = "";
            switch(number) {
                case 1:
                    listInstances();
                    break;

                case 2:
                    availableZones();
                    break;

                case 3:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.trim().isEmpty())
                        startInstance(instance_id);
                    break;

                case 4:
                    availableRegions();
                    break;

                case 5:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.trim().isEmpty())
                        stopInstance(instance_id);
                    break;

                case 6:
                    System.out.print("Enter ami id: ");
                    String ami_id = "";
                    if(id_string.hasNext())
                        ami_id = id_string.nextLine();

                    if(!ami_id.trim().isEmpty())
                        createInstance(ami_id);
                    break;

                case 7:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.trim().isEmpty())
                        rebootInstance(instance_id);
                    break;

                case 8:
                    listImages();
                    break;

                case 9:
                    String publicDns = "ec2-13-209-41-149.ap-northeast-2.compute.amazonaws.com";
                    String username = "ec2-user";
                    String privateKeyPath = "C:\\Users\\82109\\taegung.pem";
                    System.out.println("Command: condor_status");
                    executeSshCommand(publicDns, username, privateKeyPath);
                    break;

                case 10:
                    System.out.print("Enter instance id to terminate: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.trim().isEmpty())
                        terminateInstance(instance_id);
                    break;

                case 11:
                    describeSecurityGroups();
                    break;

                case 12:
                    System.out.print("Enter instance id to find attached snapshots: ");
                    if (id_string.hasNext()) {
                        String instanceId = id_string.nextLine();
                        if (!instanceId.trim().isEmpty()) {
                            snapshot(instanceId);
                        }
                    }
                    break;
                case 13:
                    describeVolumes();
                    break;

                case 14:
                    listVpcs();
                    break;

                case 15:
                    System.out.print("Enter VPC ID to list subnets: ");
                    if (id_string.hasNext()) {
                        String vpcId = id_string.nextLine();

                        if (!vpcId.trim().isEmpty()) {
                            listSubnetsByVpc(vpcId);
                        }
                    }
                    break;
                case 99:
                    System.out.println("bye!");
                    menu.close();
                    id_string.close();
                    return;

                default:
                    System.out.println("concentration!");
            }
        }
    }

    public static void listSubnetsByVpc(String vpcId) {
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();


        DescribeSubnetsRequest request = new DescribeSubnetsRequest()
                .withFilters(new Filter().withName("vpc-id").withValues(vpcId));

        try {
            DescribeSubnetsResult result = ec2.describeSubnets(request);
            List<Subnet> subnets = result.getSubnets();

            if (subnets.isEmpty()) {
                System.out.println("No subnets found for the provided VPC ID: " + vpcId);
            } else {
                System.out.println("Subnets for VPC ID " + vpcId + ":");
                for (Subnet subnet : subnets) {
                    System.out.println("Subnet ID: " + subnet.getSubnetId());
                    System.out.println("IPv4 CIDR Block: " + subnet.getCidrBlock());
                    System.out.println("State: " + subnet.getState());
                    System.out.println("------------------------------------------------------------");
                }
            }
        } catch (AmazonEC2Exception e) {
            System.out.println("Error occurred while retrieving subnets: " + e.getMessage());
        }
    }

    public static void listVpcs() {
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        try {
            DescribeVpcsRequest request = new DescribeVpcsRequest();
            DescribeVpcsResult result = ec2.describeVpcs(request);
            for (Vpc vpc : result.getVpcs()) {
                System.out.println("VPC ID: " + vpc.getVpcId());
                System.out.println("CIDR Block: " + vpc.getCidrBlock());
                System.out.println("State: " + vpc.getState());
                System.out.println("------------------------------------------------------------");
            }
        } catch (Exception e) {
            System.out.println("Failed to list VPCs: " + e.getMessage());
        }
    }


    public static void describeVolumes() {
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        try {

            DescribeVolumesRequest request = new DescribeVolumesRequest();
            DescribeVolumesResult response = ec2.describeVolumes(request);


            for (Volume volume : response.getVolumes()) {
                System.out.println("Volume ID: " + volume.getVolumeId());
                System.out.println("State: " + volume.getState());
                System.out.println("Size: " + volume.getSize() + " GiB");
                System.out.println("Volume Type: " + volume.getVolumeType());
                System.out.println("Availability Zone: " + volume.getAvailabilityZone());
                System.out.println("Created Time: " + volume.getCreateTime());
                System.out.println("------------------------------------------------------------");
            }
        } catch (Exception e) {
            System.out.println("Failed to retrieve volumes: " + e.getMessage());
        }
    }


    public static void listInstances() {

        System.out.println("Listing instances....");
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        while(!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "[id] %s, " +
                                    "[AMI] %s, " +
                                    "[type] %s, " +
                                    "[state] %10s, " +
                                    "[monitoring state] %s",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
                System.out.println();
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }
    }

    public static void availableZones()	{

        System.out.println("Available zones....");
        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();

            AvailabilityZone zone;
            while(iterator.hasNext()) {
                zone = iterator.next();
                System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
            }
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

    }

    public static void startInstance(String instance_id)
    {

        System.out.printf("Starting .... %s\n", instance_id);
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StartInstancesRequest> dry_request =
                () -> {
                    StartInstancesRequest request = new StartInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instance_id);

        ec2.startInstances(request);

        System.out.printf("Successfully started instance %s", instance_id);
    }


    public static void availableRegions() {

        System.out.println("Available regions ....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeRegionsResult regions_response = ec2.describeRegions();

        for(Region region : regions_response.getRegions()) {
            System.out.printf(
                    "[region] %15s, " +
                            "[endpoint] %s\n",
                    region.getRegionName(),
                    region.getEndpoint());
        }
    }

    public static void stopInstance(String instance_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StopInstancesRequest> dry_request =
                () -> {
                    StopInstancesRequest request = new StopInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        try {
            StopInstancesRequest request = new StopInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.stopInstances(request);
            System.out.printf("Successfully stop instance %s\n", instance_id);

        } catch(Exception e)
        {
            System.out.println("Exception: "+e.toString());
        }

    }

    public static void createInstance(String ami_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        RunInstancesRequest run_request = new RunInstancesRequest()
                .withImageId(ami_id)
                .withInstanceType(InstanceType.T2Micro)
                .withMaxCount(1)
                .withMinCount(1);

        RunInstancesResult run_response = ec2.runInstances(run_request);

        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

        System.out.printf(
                "Successfully started EC2 instance %s based on AMI %s",
                reservation_id, ami_id);

    }

    public static void rebootInstance(String instance_id) {

        System.out.printf("Rebooting .... %s\n", instance_id);

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        try {
            RebootInstancesRequest request = new RebootInstancesRequest()
                    .withInstanceIds(instance_id);

            RebootInstancesResult response = ec2.rebootInstances(request);

            System.out.printf(
                    "Successfully rebooted instance %s", instance_id);

        } catch(Exception e)
        {
            System.out.println("Exception: "+e.toString());
        }


    }

    public static void listImages() {
        System.out.println("Listing images....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeImagesRequest request = new DescribeImagesRequest();
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

        request.getFilters().add(new Filter().withName("name").withValues("aws-htcondor-slave"));
        request.setRequestCredentialsProvider(credentialsProvider);

        DescribeImagesResult results = ec2.describeImages(request);

        for(Image images :results.getImages()){
            System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
                    images.getImageId(), images.getName(), images.getOwnerId());
        }
    }

    public static void executeSshCommand(String publicDns, String username, String privateKeyPath) {
        String command = "condor_status";
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(privateKeyPath);
            Session session = jsch.getSession(username, publicDns, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);

            System.out.println("Executing SSH Command: " + command);
            channel.connect();

            Scanner scanner = new Scanner(channel.getInputStream());
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }

            scanner.close();
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            System.out.println("Error during SSH command execution: " + e.getMessage());
        }
    }


    public static void terminateInstance(String instanceId) {
        System.out.printf("Terminating instance: %s\n", instanceId);

        TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest()
                .withInstanceIds(instanceId);

        TerminateInstancesResult terminateResponse = ec2.terminateInstances(terminateRequest);

        System.out.println("Successfully terminated instance: " + instanceId);
    }

    public static void describeSecurityGroups() {
        System.out.println("Describing security groups...");

        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
        DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(request);

        for (SecurityGroup group : result.getSecurityGroups()) {
            System.out.printf("[GroupID] %s, [GroupName] %s\n", group.getGroupId(), group.getGroupName());
        }
    }

    public static void snapshot(String instanceId) {
        System.out.printf("Listing EBS snapshots attached to instance: %s\n", instanceId);

        // Describe the instance to get block device mappings
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult result = ec2.describeInstances(request);

        // Iterate over the reservations and instances
        for (Reservation reservation : result.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                // Check the block device mappings of the instance
                for (InstanceBlockDeviceMapping blockDeviceMapping : instance.getBlockDeviceMappings()) {
                    String deviceName = blockDeviceMapping.getDeviceName();
                    Volume volume = ec2.describeVolumes(new DescribeVolumesRequest()
                            .withVolumeIds(blockDeviceMapping.getEbs().getVolumeId())).getVolumes().get(0);

                    System.out.printf("[Device] %s, [VolumeID] %s, [SnapshotID] %s, [State] %s\n",
                            deviceName, volume.getVolumeId(), volume.getSnapshotId(), volume.getState());
                }
            }
        }
    }


}
