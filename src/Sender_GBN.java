/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Student
 */
/// Application, presentation, session, transport, network, data link, physical
/// A-HP-HS-HT-HN-HD-HPh-HThisIsTheFreakingDataD-T
public class Sender_GBN extends Thread
{
    int Sw = 15, Sf, Sn, cur_packet_no = -1;

    String divisor = "100000100110000010001110110110111";

    int lst_packet;
    int final_packet;
    String packet[] = new String [1000];
    Map h1 = new HashMap<String,String>();
    Map four1 = new HashMap<String,String>();
    Queue Q = new LinkedList();

    BufferedWriter bw, bw1 ;
    int encoding;

    ServerSocket serverSocket = null;
    Socket socket = null;
    DataInputStream in = null;
    DataOutputStream out = null;
    int PORT = 8098;

	boolean alldone = false;
    Lock queue_lock = new ReentrantLock();
    public Sender_GBN(File file) throws IOException, InterruptedException
    {
        //System.out.println("Send shuru");

        /*
         * Code for initializing sockets
         */
        try
        {
            //System.out.println("Ekhane");
            serverSocket = new ServerSocket(PORT);
            //System.out.println("okhane");
            socket = serverSocket.accept();
            //System.out.println("JEkhane");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            System.out.println("serversocket cannot accept");
        }

        try
        {
            in = new DataInputStream(socket.getInputStream());
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

        try
        {
            out = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            System.out.println(e);
        }



        /*
         * Next code
         */

        Reader reader = new InputStreamReader(new FileInputStream(file));

        File fin = new File("ham.txt");
        File fin1 = new File("fourB_fiveB.txt");
        Scanner sc = new Scanner(fin);
        Scanner sc1 = new Scanner(fin1);

        String s = "", s1 = "";
        while(true)
        {
            if(sc.hasNext())
                s = sc.next();
            else
                break;

            s1 = sc.next();
            h1.put(s, s1);
        }

        while(true)
        {
            if(sc1.hasNext())
                s = sc1.next();
            else
                break;

            s1 = sc1.next();
            four1.put(s, s1);
        }
        new Thread(Send).start();
        new Thread(ack_receive).start();
        // creates a FileWriter Object

        // Writes the content to the file
        // if file doesnt exists, then create it

        int flag = 0;
        boolean EndOfFile = false;
        lst_packet = 0;
        while(true)
        {
            String str = "";
            cur_packet_no++;
            for(int i = 0; i<125; i++)
            {
                int ch = reader.read();
                if (ch != -1)
                {
                    // check for EOF
                    // we have a character ...

                    str+=(char)ch;

                }
                else
                {
                    for(; i<125; i++)
                        str+="#";
                    EndOfFile = true;
                    break;
                }
            }

            application(str);
            if(EndOfFile)
            {
               // System.out.println("final packet is sent ");
                cur_packet_no++;
                str = "#";
                application(str);
                break;
            }
        }
     //   System.out.println("Send shesh!");
    }

    void application(String line) throws IOException, InterruptedException
    {
        line= "A-H"+line;
        transport(line);
    }

    void transport(String line) throws IOException, InterruptedException
    {
        line= "T-H"+line;
        network(line);
    }

    void network(String line) throws IOException, InterruptedException
    {
        line= "N-H"+line;
        DataLink(line);
    }

    void DataLink(String line) throws IOException, InterruptedException
    {
        line= "D-H"+line+"D-T";
        line = toBinary(line);
        line = SelectErrorDetectionCorrectionMode(GUI.ErrorMode, line);
        line = fourB_fiveB(line);
        line = makeAddress(cur_packet_no) + line;
        while(Q.size() >= Sw)
            Thread.sleep(100);
        //while(queue_lock.tryLock() == false);
        queue_lock.lock();
        Q.add(line);
        queue_lock.unlock();
        
    }

    String InsertError(String line)
    {
        // TODO Auto-generated method stub
        if(GUI.ErrorMode.equals("CRC-32"))
            return Insert_error_CRC(line);
        else
            return Insert_error_Hamming(line);


    }

    String makeAddress(int packet_no)
    {
        String ret = "";
        int nw = packet_no;

        for(int i = 0; i<10; i++)
        {
            if(nw%2 == 1)
                ret+='1';
            else
                ret+='0';

            nw/=2;
        }

        ret = new StringBuilder(ret).reverse().toString();
        return ret;
    }

    String SelectEncodingScheme(String encoding2, String line)
    {
        // TODO Auto-generated method stub

        if(encoding2.equals("NRZ-L"))
            return NRZL(line);
        else if(encoding2.equals("NRZ-I"))
            return NRZI(line);
        else if(encoding2.equals("RZ"))
            return RZ(line);
        else if(encoding2.equals("Manchester"))
            return Manchester(line);
        else
            return differential_Manchester(line);


    }

    private String SelectErrorDetectionCorrectionMode(String errorMode, String str)
    {
        // TODO Auto-generated method stub

        if(errorMode.equals("CRC-32"))
            return CRC(str);
        else
            return Hamming(str);

    }


    String toBinary(String s)
    {
        String ret = "";
        for(int i = 0; i<s.length(); i++)
        {
            int c = (int)s.charAt(i);
            String tmp = "";
            for(int j = 0; j<8; j++)
            {
                if(c%2 == 1)
                    tmp+='1';
                else
                    tmp+='0';
                c/=2;
            }

            tmp = new StringBuilder(tmp).reverse().toString();
            ret += tmp;
        }

        return ret;
    }
    static String NRZL(String s)
    {
        String ret = "";
        for(int i = 0; i<s.length(); i++)
        {
            if(s.charAt(i) == '1')
                ret += '+';
            else
                ret += '-';
        }
        return ret;
    }
    static String NRZI(String s)
    {
        String ret = "+";
        char nw = '+';
        for(int i = 0; i<s.length(); i++)
        {
            if(s.charAt(i) == '1')
            {
                if(nw == '+')
                    nw = '-';
                else
                    nw = '+';
            }
            ret += nw;
        }
        return ret;

    }
    static String RZ(String s)
    {
        String ret = "";
        for(int i = 0; i<s.length(); i++)
        {
            if(s.charAt(i) == '1')
                ret += "+";
            else
                ret += "-";
            ret+='0';
        }
        return ret;
    }
    static String Manchester(String s)
    {
        String ret = "";
        for(int i = 0; i<s.length(); i++)
        {
            if(s.charAt(i) == '1')
                ret += "+-";
            else
                ret += "-+";
        }
        return ret;
    }

    static String differential_Manchester(String s)
    {
        String ret = "+-";
        String nw = "+-";
        for(int i = 0; i<s.length(); i++)
        {
            if(s.charAt(i) == '1')
            {
                if(nw == "+-")
                    nw = "-+";
                else
                    nw = "+-";
            }
            ret += nw;
        }
        return ret;
    }

    String fourB_fiveB(String str)
    {
        String ret = "";
        for(int i = 0; i< str.length();)
        {
            String temp = "";
            for(int j = i, k = 0; k<4; i++, j++, k++)
            {
                temp+=str.charAt(j);
            }

            ret+= four1.get(temp);
        }

        return ret;
    }


    String Hamming(String str)
    {
        String ret = "";
        for(int i = 0; i< str.length();)
        {
            String temp = "";
            for(int j = i, k = 0; k<4; i++, j++, k++)
            {
                temp+=str.charAt(j);
            }

            ret+= h1.get(temp);
        }

        return ret;
    }

    static String Insert_error_Hamming(String str)
    {
        Random rand = new Random();
        String temp1 = "";

        int a = rand.nextInt()%100+1;
        if(a<0)
            a = (-1)*a;
        if(a <= 5)
        {
            int b = rand.nextInt()%2+1;
            if(b<0)
                b = (-1)*b;
            for(int j = 0; j<b; j++)
            {
                int c = rand.nextInt()%str.length();
                if(c<0)
                    c = (-1)*c;
                char rep ;
                if(b==1)
                {
                    if(str.charAt(c)=='0')
                        rep = '1';
                    else
                        rep = '0';
                    str = str.substring(0, c)+rep+str.substring(c+1, str.length());
                }
                else
                {
                    if(str.charAt(c)=='0')
                        rep = 'a';
                    else
                        rep = 'b';
                    str = str.substring(0, c)+rep+str.substring(c+1, str.length());
                }
            }
        }
        return str;
    }


    String XOR(String a, String b)
    {
        String ret = "";
        for(int i = 0; i< a.length(); i++)
        {
            if(a.charAt(i) == b.charAt(i))
                ret+='0';
            else
                ret+='1';
        }

        return ret;
    }

    String divide(String dividend, String divisor)
    {
        String temp  = dividend.substring(0, 32);
        for(int i = 32; i<dividend.length(); i++)
        {
            temp+= dividend.charAt(i);
            if(temp.charAt(0) == '0')
                temp = temp.substring(1, 33);
            else
            {
                temp = XOR(temp, divisor);
                temp = temp.substring(1, 33);
            }
        }
        return temp;
    }

    String CRC(String str)
    {
        String dividend = str;
        for(int i= 0; i<32; i++)
            dividend+='0';

        String code = divide(dividend, divisor);
        str+=code;
        return str;
    }

    String Insert_error_CRC(String str)
    {
        Random random = new Random();
        int a = random.nextInt()%100+1;
        if(a<0)
            a = a*-1;

        if(a<=5)
        {
            int p = random.nextInt()%2+1;
            if(p<0)
                p*=-1;
            int q;
            if(p == 1)
                q = 1;
            else
            {
                q = random.nextInt()%20+1;
                if(q <0)
                    q*=-1;
            }
           // System.out.println("error is inserted in this packet");
            for(int i = 0; i<q; i++)
            {
                int b = random.nextInt() %str.length();
                if(b<0)
                    b = b*-1;

                if(str.charAt(b) == '0')
                    str = str.substring(0, b)+ '1'+ str.substring(b+1, str.length());
                else
                    str = str.substring(0, b)+ '0'+ str.substring(b+1, str.length());
            }
        }

        return str;
    }

    int get_address(String s)
    {
    	int ret = 0, a = 1;
    	for(int i = s.length()-1; i>= 0; i--)
    	{
    		if(s.charAt(i) == '1')
    			ret += a;
    		a*=2;
    	}
    	return ret;
    }
    
    Runnable Send = new Runnable()
    {
        long lst_send_time = -100000;

        void physical(String line) throws IOException
        {
        	String tmp = line.substring(0, 10);
        	line = line.substring(10);
        	line = InsertError(line);
            line= SelectEncodingScheme(GUI.Encoding, line);
            line = tmp + line;
            //System.out.println("Sent packet is " + line);
            try
            {
            	//System.out.println("Size of line is " + line.length());
                if(line.length() < 1000)
                	line += (char)200;
            	out.writeBytes(line);
            }
            catch (IOException e)
            {
                System.out.println("Client disconnected");
            }
        }

        public void run()
        {
            while(true)
            {
            	while(Q.isEmpty())
                {
                	if(alldone)
                		break;
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            	if(alldone)
            		break;
            	long time_since_last_send = System.currentTimeMillis() - lst_send_time;
                if(time_since_last_send > 500)
                {
                	//while(queue_lock.tryLock() == false);
                	queue_lock.lock();
                    int total_pres_in_queue = Q.size(), sent = 0;
                    lst_send_time = System.currentTimeMillis();
                    while(sent < total_pres_in_queue)
                    {
                        String nw = (String) Q.peek();
                        Q.remove();
                        Q.add(nw);
                        try
                        {
                            physical(nw);
                        }
                        catch (IOException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        sent++;
                    }
                    queue_lock.unlock();
                }
            }
            for(int i = 1; i<=500000000; i++);
    		System.out.println("Terminating Send");
    		try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    };
    
    Runnable ack_receive = new Runnable()
    {
    	public void run()
    	{
    		while(true)
    		{
    			int ch = -1;
    			while(ch == -1)
				{
    				try 
    				{
						ch = in.read();
					} catch (IOException e) 
    				{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if(ch == 200)
				{
					alldone = true;
					while(Q.isEmpty() == false)
						Q.poll();
					break;
				}
    			String address = "";
    			address += (char) ch;
    			for(int i = 1; i<10; i++)
				{
    				try 
    				{
						address += (char)in.read();
					} catch (IOException e) 
    				{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
    			//System.out.println("ack received address is " + address);
    			int addrs = get_address(address);
    			//System.out.println("ack address = " + addrs);
    			//while(queue_lock.tryLock() == false);
    			queue_lock.lock();
    	        while(Q.isEmpty() == false)
    			{
    				String nw = (String)Q.peek();
    				int pres_addr = get_address(nw.substring(0, 10));
    				//System.out.println("pres_addr is " + pres_addr);
    				if(pres_addr > addrs)
    					break;
    				Q.remove();
    			}
    	        //System.out.println("Queue size is " + Q.size());
    			queue_lock.unlock();
    			
    		}
    		System.out.println("Terminating ack_receive");
    	}
    	
    };
}

