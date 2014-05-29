import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class WaitThread implements Runnable{

	/** Constructor */
	public WaitThread() {
	}

	@Override
	public void run() {
		waitForConnection();
	}

	/** Waiting for connection from devices */
	private void waitForConnection() {
		// retrieve the local Bluetooth device object
		LocalDevice local = null;
		
		StreamConnectionNotifier notifier;
		StreamConnection connection = null;

		// setup the server to listen for connection
		try {
			local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);

			//UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
			//UUID uuid = new UUID("04c6093b-0000-1000-8000-00805f9b34fb", true);
			//UUID uuid = new UUID("CAA6519E-F75E-2AC0-B7B8-F80348BC3FC3", false);
			//java.util.UUID m_UID = java.util.UUID.fromString("CAA6519E-F75E-2AC0-B7B8-F80348BC3FC3");
			String s_uuid = "CAA6519EF75E2AC0B7B8F80348BC3FC3";
			
			System.out.println(s_uuid);
			String url = "btspp://localhost:" + s_uuid + ";name=RemoteBluetooth";
			System.out.println(url);
			notifier = (StreamConnectionNotifier)Connector.open(url);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
       	       	// waiting for connection
		while(true) {
			try {
				System.out.println("waiting for connection...");
	                  	connection = notifier.acceptAndOpen();

				Thread processThread = new Thread(new ProcessConnectionThread(connection));
				processThread.start();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
}