import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;


public class ProcessConnectionThread implements Runnable{

	private StreamConnection mConnection;

	// Constant that indicate command from devices
	private static final int EXIT_CMD = -1;
	private static final int KEY_RIGHT = 1;
	private static final int KEY_LEFT = 2;
	
	// action flags for touch pad
	private static final int ACTION_LEFT_BUTTON_CLICK = 0;
	private static final int ACTION_LEFT_BUTTON_DOWN = 1;
	private static final int ACTION_LEFT_BUTTON_UP = 2;
	
	private static final int ACTION_RIGHT_BUTTON_CLICK = 3;
	private static final int ACTION_RIGHT_BUTTON_DOWN = 4;
	private static final int ACTION_RIGHT_BUTTON_UP = 5;
	
	private static final int ACTION_MOUSE_MOVE = 6;
	private static final int ACTION_SCROLL_MOVE = 7;
	
	private static final int ACTION_TEST_MESSAGE = 8;
	
	private int move_x;
	private int move_y;
	
	private int flag_x;
	private int flag_y;
	
	private int diff_dist;
	private int diff_flag;
	
	private String receviedMsg;

	public ProcessConnectionThread(StreamConnection connection)
	{
		mConnection = connection;
	}

	@Override
	public void run() {
		try {
			OutputStream outputStream = mConnection.openOutputStream();
			String x = "Connection established!";
			outputStream.write(x.getBytes());
			outputStream.flush();
			// prepare to receive data
			InputStream inputStream = mConnection.openInputStream();
			//DataInputStream dataInputStream =  mConnection.openDataInputStream();
//			BufferedReader bfReader = new BufferedReader(new InputStreamReader(inputStream));
			System.out.println("waiting for input");
		
			
			while (true) {
				int command = inputStream.read();  
				//System.out.println("Command: "+command);
				if (command == EXIT_CMD)  
                {  
                    System.out.println("finish process");  
                    break;  
                }  
				
				if (command == ACTION_MOUSE_MOVE)
				{
					flag_x = inputStream.read();					
					move_x = inputStream.read();
					
					if (flag_x == 1) move_x = -move_x;
					flag_y = inputStream.read();
					move_y = inputStream.read();
					if (flag_y == 1) move_y = -move_y;
				}
				
				if (command == ACTION_SCROLL_MOVE)
				{
					diff_flag = inputStream.read();
					diff_dist = inputStream.read();
					if (diff_flag == 1) diff_dist = -diff_dist;
				//	System.out.println("Diff_flag: "+diff_flag);
				//	System.out.println("Diff_dist: "+diff_dist);
				}
				
				if (command == ACTION_TEST_MESSAGE)
				{
					int count = 0;
					while(count == 0){
						count = inputStream.available();
					}
					
					if( count != 0 ){
						//System.out.println(count);
						byte[] bt = new byte[count];
						int readCount = 0;
						while(readCount < count){
							readCount += inputStream.read(bt, readCount, count-readCount);
						}
						//System.out.println(readCount);
						receviedMsg = new String(bt);
						
					}
				}

//				

//				int x1 = inputStream.read();
//				System.out.println(x1);
//				int y1 = inputStream.read();
//				System.out.println(y1);
//				Robot robot1 = new Robot();
//				robot1.mouseMove(x1, y1);
//				System.out.println("---------");
				processCommand(command);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process the command from client
	 * @param command the command code
	 */
	private void processCommand(int command) {
		
		try {
			Robot robot = new Robot();
			Clipboard cliper = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection sSelection = null;
			switch (command) {
	    		case ACTION_LEFT_BUTTON_CLICK:
	    			robot.mousePress(InputEvent.BUTTON1_MASK);	
	    			robot.mouseRelease(InputEvent.BUTTON1_MASK);
	    			
	    			break;
	    		case ACTION_LEFT_BUTTON_DOWN:
	    			robot.mousePress(InputEvent.BUTTON1_MASK);	    			
	    			break;
	    		case ACTION_LEFT_BUTTON_UP:
	    			robot.mouseRelease(InputEvent.BUTTON1_MASK);
	    			break;
	    		case ACTION_RIGHT_BUTTON_CLICK:
	    			robot.mousePress(InputEvent.BUTTON3_MASK);
	    			robot.mouseRelease(InputEvent.BUTTON3_MASK);	    
	    			break;
	    		case ACTION_RIGHT_BUTTON_DOWN:
	    			robot.mousePress(InputEvent.BUTTON3_MASK);
	    			break;
	    		case ACTION_RIGHT_BUTTON_UP:
	    			robot.mouseRelease(InputEvent.BUTTON3_MASK);
	    			break;
	    		case ACTION_MOUSE_MOVE:
	    			Point pt = MouseInfo.getPointerInfo().getLocation();
	    			robot.mouseMove(pt.x + move_x, pt.y+move_y);
	    			break;
	    		case ACTION_SCROLL_MOVE:
	    			System.out.println("Scroll with "+diff_dist);
	    			robot.mouseWheel(diff_dist/2);
	    			break;
	    		case ACTION_TEST_MESSAGE:
	    			sSelection = new StringSelection(receviedMsg);
	    			cliper.setContents(sSelection, null);
	    			robot.keyPress(KeyEvent.VK_CONTROL);
	    			robot.keyPress(KeyEvent.VK_V);
	    			
	    			robot.keyRelease(KeyEvent.VK_CONTROL);
	    			robot.keyRelease(KeyEvent.VK_V);
	    			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}