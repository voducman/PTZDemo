package Connection.PTZCamera;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.xml.soap.SOAPException;


import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.MediaDevices;
import de.onvif.soap.devices.PtzDevices;
import de.onvif.soap.devices.ImagingDevices;

import org.onvif.ver10.schema.Profile;
import org.onvif.ver10.schema.FloatRange;
import org.onvif.ver10.schema.Capabilities;
import org.onvif.ver10.schema.PTZNode;


public class Main {
    private static float zoom = 0f;

    public static void main(String[] args){
        try {
            // Connect to Onvif camera (onvif account)
            OnvifDevice  nvt = new OnvifDevice("115.78.5.10:9080", "onvif", "ViettelRD123aB");
            // Get datetime from Onvif camera (No authentication required)
            Date nvtDate = nvt.getDevices().getDate();
            System.out.println(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(nvtDate));

            MediaDevices mediaDevices = nvt.getMedia();
            PtzDevices   ptzDevices   = nvt.getPtz();
            ImagingDevices imagingDevices = nvt.getImaging();
            Capabilities capabilities = nvt.getDevices().getCapabilities();

            List<Profile> profiles = nvt.getDevices().getProfiles();
            String  profileToken = profiles.get(0).getToken();


            System.out.println("Media RTSP Streaming URI: " + mediaDevices.getRTSPStreamUri(profileToken));
            System.out.println("Media HTTP Streaming URI: " + mediaDevices.getHTTPStreamUri(profileToken));
            System.out.println("Snapshot URI: " + mediaDevices.getSnapshotUri(profileToken));

            if (ptzDevices.isPtzOperationsSupported(profileToken)){
                System.out.println("PTZ is supported");
                String serverPtzUri = capabilities.getPTZ().getXAddr();
                System.out.println("PTZ URI: " + serverPtzUri);

                PTZNode ptzNode = ptzDevices.getNode(profileToken);
                FloatRange panRange = ptzDevices.getPanSpaces(profileToken);
                FloatRange tilRange = ptzDevices.getTiltSpaces(profileToken);
                FloatRange zomRange = ptzDevices.getZoomSpaces(profileToken);
                float zoom = zomRange.getMax();

                // Retain the camera position
                float panPos = ptzDevices.getPosition(profileToken).getPanTilt().getX();
                float tilPos = ptzDevices.getPosition(profileToken).getPanTilt().getY();
                float zomPos = ptzDevices.getPosition(profileToken).getZoom().getX();

                System.out.println("Pan Position:  " + panPos);
                System.out.println("Tilt Position:  " + tilPos);
                System.out.println("Zoom Position:  " + zomPos);

                System.out.println("pan max: " + panRange.getMax());
                System.out.println("pan min: " + panRange.getMin());
                System.out.println("til max: " + tilRange.getMax());
                System.out.println("til min: " + tilRange.getMin());
                System.out.println("zoom max: " + zomRange.getMax());
                System.out.println("zoom min: " + zomRange.getMin());

                for (int i = 0; i<20; i++){

                    panPos = ptzDevices.getPosition(profileToken).getPanTilt().getX();
                    tilPos = ptzDevices.getPosition(profileToken).getPanTilt().getY();
                    zomPos = ptzDevices.getPosition(profileToken).getZoom().getX();

                    // Absolute movement
                    if (ptzDevices.isAbsoluteMoveSupported(profileToken)){
                        boolean isSuccess = ptzDevices.absoluteMove(profileToken, panPos, tilPos, changZoom());
                        System.out.println("Absolute Move return code: " + isSuccess);
                    }else{
                        System.out.println("Absolute Move not supported");
                    }

                    // Relative movement
                    Random random = new Random();

                    //float x = (panRange.getMax() + panRange.getMin())/2f;
                    //float y = (tilRange.getMax() + tilRange.getMin())/2f;
                    float x_rand = random.nextFloat()*2 - 1; // [-1:1]
                    float y_rand = random.nextFloat()*2 - 1; // [-1:1]

                    if (ptzDevices.isRelativeMoveSupported(profileToken)){
                        boolean isSuccess = ptzDevices.relativeMove(profileToken, x_rand, y_rand, zomPos);
                        System.out.println("Relative Move return code: " + isSuccess);
                    }else{
                        System.out.println("Relative Move not supported");
                    }

                    try{
                        Thread.sleep(5000);
                    }catch (InterruptedException e){
                        System.out.println(e.getStackTrace());
                    }
                }




            }

            //System.out.println("Imaging URI: " + imagingDevices.getImagingSettings(profiles.get(0).getVideoSourceConfiguration().getSourceToken()));



        }catch (ConnectException e){
            // Exception is throwed if cannot connect on network
            System.err.println("Could not connect to NVT.");
        }catch (SOAPException e){
            // Exception is throwed if SOAP handle get an error at NVT Device
            System.out.println("SOAP Exception");
            e.printStackTrace();
        }
    }

    public static float changZoom(){
        zoom += 0.2f;
        zoom = (zoom == 1.0f)? 0f: zoom;
        return zoom;
    }
}
