/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Student
 */
/// Application, presentation, session, transport, network, data link, physical
/// A-HP-HS-HT-HN-HD-HPh-HThisIsTheFreakingDataD-T
public class Receiver_GBN
{
    Reader reader;
    int packet_size, total = 0, encoding;
    String divisor = "100000100110000010001110110110111";
    String List[] = new String[16];
    Map h2 = new HashMap<String,String>();
    Map four2 = new HashMap<String,String>();
    static String HOME = "127.0.0.1"; //there is no place like this
    static int PORT = 8098;
    static DataInputStream in = null;
    boolean[] packet_received = new boolean[1010];
    int cur_packet = 0;
    String address = "";
    static DataOutputStream out_ack = null;
    
    public Receiver_GBN() throws IOException
    {
    	int SS, n;
                Socket socket = null;
        try {
            socket = new Socket(HOME, PORT);
            in = new DataInputStream(socket.getInputStream());
            out_ack = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        Random rand = new Random();
        File file2 = new File("Out.txt");
       
       
        FileWriter writer = new FileWriter(file2);
       
        // Writes the content to the file
        // if file doesnt exists, then create it
        File fin = new File("ham.txt");
        File fin1 = new File("fourB_fiveB.txt");
        
        Scanner sc = new Scanner(fin);
        Scanner sc1 = new Scanner(fin1);
        
        
        String s = "", s1 = "";
        int x = 0;
        while(true)
        {
            if(sc.hasNext())
               s = sc.next();
            else
                break;

            s1 = sc.next();
            List[x++] = s1;
            h2.put(s1, s);
           


        }
        
        while(true)
        {
            if(sc1.hasNext())
               s = sc1.next();
            else
                break;

            s1 = sc1.next();
           
            four2.put(s1, s);
           


        }

       	int packet_size = getPacketSize();
        boolean EndOfFile = false;
       
        
        while(true)
        {
        	EndOfFile = false;
            drop = false;
            String str = "";
            int i;
            int ch = -1;
            while(ch == -1)
        	{
        		ch = in.read();
        	//	System.out.println("I'm stuck here");
        	}
            str += (char)ch;
            for(i = 1; i<packet_size; i++)
            {
            	ch = in.read();
            	//System.out.println(i + " " + ch);
                if (ch != -1 && ch!= 200)
                {
                    // check for EOF
                    // we have a character ...
                    str+=(char)ch;
                }
                else
                {
                	//System.out.println("I'm here to break everything");
                    EndOfFile = true;
                    break;
                }
            }
            //System.out.println("Value of EndOfFile " + EndOfFile);
            

           
            address = str.substring(0,10);
            //System.out.println("Received packet is " + str);
            str = str.substring(10);
            //System.out.println("Stripped packet is " + str);
            int addrs = toInt(address);
           // System.out.println("address of the current packet is " + addrs);
            if(addrs!=cur_packet)
            {
            	//System.out.println(" Packet dropped as the expected one is not received");
            	continue;
            }
           // System.out.println(str);
            if(EndOfFile)
            {
            	out_ack.writeByte(200);
            	break;
            }
            str = Physical(str);
            
            //System.out.println("This is current "+ cur_packet);
            //System.out.println(str);
            
           if(!str.equals("-"))
           {
        	  // 	System.out.println("I'm writing this");
        	  // 	System.out.println(str);
        	   	writer.write(str);
           		cur_packet++;
           }
        //   else
    //       {
        	 //  System.out.println("Packet dropped due to errors");
    //       }
           
        }
       System.out.println("Everything's finished");
       writer.flush();
       writer.close();
    }
    
   
    int getPacketSize()
    {
    	if(GUI.ErrorMode.equals("CRC-32"))
    	{
    		if(GUI.Encoding.equals("NRZ-L"))
    			return 1440+10;
    		else if(GUI.Encoding.equals("NRZ-I"))
    			return 1441+10;
    		else if(GUI.Encoding.equals("RZ"))
    			return 2880+10;
    		else if(GUI.Encoding.equals("Manchester"))
    			return 2880+10;
    		else
    			return 2882+10;
    		
    	}
    	
    	else
    	{
    		if(GUI.Encoding.equals("NRZ-L"))
    			return 2450+10;
    		else if(GUI.Encoding.equals("NRZ-I"))
    			return 2451+10;
    		else if(GUI.Encoding.equals("RZ"))
    			return 4900+10;
    		else if(GUI.Encoding.equals("Manchester"))
    			return 4900+10;
    		else
    			return 4902+10;
    		
    	}
    }
    String Application(String s)
    {
        s = s.substring(3);
        return s;
    }
    
    String Transport(String s)
    {
        s = s.substring(3);
        return Application(s);
    }
    String Network(String s)
    {
        s = s.substring(3);
        return Transport(s);
    }
    String DataLink(String s) throws IOException
    {
    	s = fourB_fiveB(s);
    	if(s.equals("-"))
    	{
    	//	System.out.println("Drop hoise EIKHANE! :v");
    		return s;
    	}
    	s= SelectErrorDetectionCorrectionMode(GUI.ErrorMode, s);
    	if(s.equals("-"))
    	{
    //		System.out.println("Drop hoise. :v");
    		return s;
    	}
    	else
    	{
    		s = toDecimal(s);
	        s = s.substring(3);
	        s = s.substring(0,s.length()-3);
	    	///send ack
	      //  System.out.println("address is " + address);
	        out_ack.writeBytes(address);
	        return Network(s);
    	}
    }
    String Physical(String s) throws IOException
    {
    	s = SelectEncodingScheme(GUI.Encoding, s); 
        return DataLink(s);
    }
    
    
    // Some Additional Functions...
    private String SelectErrorDetectionCorrectionMode(String errorMode, String str) {
		// TODO Auto-generated method stub
    	
    	if(errorMode.equals("CRC-32"))
    		return CRC(str);
    	else
    		return Hamming(str);
		
	}
    
	private String SelectEncodingScheme(String encoding2, String line) {
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
	
	 String toDecimal(String str)
	    {
	        String ret = "";
	        for(int i = 0; i< str.length(); i+=8)
	        {
	            int a = 0, b;

	            for(int j = i+7, k = 1; j>=i; j--,k*=2)
	            {
	                if(str.charAt(j) == '1')
	                    a+=k;
	            }
	           
	            ret+=(char)a;
	        }
	        return ret;
	    }
	 
	 int toInt(String binary)
	    {
	    	int a = 1;
	    	int ret = 0;
	    	for(int i = binary.length()-1 ; i>=0; i--)
	    	{
	    		if(binary.charAt(i) == '1')
	    			ret+=a;
	    		a*=2;
	    				
	    	}
	    	
	    	return ret;
	    }
	 
	    

	
	// Line Decoding...
    String NRZL(String s)
    {
        String ret = "";
        for(int i = 0; i<s.length(); i++)
        {
           if(s.charAt(i) =='+')
        	   ret+='1';
           else
        	   ret+='0';
        }
        return ret;
    }
    String NRZI(String str)
	{
		String ret = "";
		char last = str.charAt(0);
		for(int i = 1; i< str.length(); i++)
		{
		
				if(str.charAt(i) == last)
				{
					ret+='0';
				}
				else
				{
					ret+='1';
					last = str.charAt(i);
				}
			
		}
		
			return ret;
			
	}
	
	String RZ(String str)
	{
		String ret = "";
		for(int i = 0; i< str.length(); i+=2)
		{
			if(str.charAt(i) == '-')
				ret+='0';
			else
				ret+='1';
		}
		
		return ret;
	}
	
	String Manchester(String str)
	{
		String ret = "";
		for(int i = 0; i< str.length(); i+=2)
		{
			if(str.charAt(i) == '-')
				ret+='0';
			else
				ret+='1';
		}
		
		return ret;
	}
	
	String differential_Manchester(String str)
	{
		String ret = "";
		char s = str.charAt(0);
		String last = "";
		last+=s;
		s = str.charAt(1);
		last+=s;
		
		for(int i = 2; i< str.length(); i+=2)
		{
			s = str.charAt(i);
			String temp = "";
			temp+=s;
			s = str.charAt(i+1);
			temp+=s;
		
				if(temp.equals(last))
				{
					ret+='0';
				}
				else
				{
					ret+='1';
					last = temp;
				}
			
		}
		
			return ret;
			
	}
	
	//Block Decoding...
   
    String fourB_fiveB(String str)
    {
        String ret = "";
        boolean dropped = false; 
       // System.out.println(str.length());
        for(int i = 0; i<str.length();)
        {
         
            String temp1 = "", temp2 = "";
           
            for(int j = i, k = 0; k<5 ; i++, j++, k++)
            {
                temp1+=str.charAt(j);

            }
           
            for(int j = i, k = 0; k<5 ; i++, j++, k++)
            {
                temp2+=str.charAt(j);

            }
             if(four2.containsKey(temp1) && four2.containsKey(temp2))
            { 
                ret+=four2.get(temp1);
                ret+=four2.get(temp2);
            }
            else
            {   
            	dropped = true;
            	break;
          
            }
        }

       // System.out.println(ret);
        if(dropped)
        	return "-";
        else;
        	return ret;

    }
   //Hamming
   
    String Check_Hamming(String str)
    {
        String ret = "";
        for(int i = 0; i<16; i++)
        {
            int cnt = 0;
            for(int j = 0; j<7; j++)
            {
                if(List[i].charAt(j) != str.charAt(j))
                    cnt++;
            }
            if(cnt == 0 || cnt == 1)
            {
               
               str = List[i];
               break;
            }
        }
        
        return str;
    }
    String CorrectError(String str)
    {
     
        String ret = "";
        int nw = 1;
        for(int i = 0; i<str.length();)
        {
              
            String temp = "";

            for(int j = i, k = 0; k<7; j++, k++,i++)
            {
                temp+=str.charAt(j);
            }
             temp = Check_Hamming(temp);
                
            
            ret+=temp;


        
        }

       
        
      
        return ret;

    }

    String Hamming(String str)
    {
        String ret = "";
        boolean dropped = false;
       // System.out.println(str.length());
        for(int i = 0; i<str.length();)
        {
         
            String temp1 = "", temp2 = "";
           
            for(int j = i, k = 0; k<7 ; i++, j++, k++)
            {
                temp1+=str.charAt(j);

            }
           
            for(int j = i, k = 0; k<7 ; i++, j++, k++)
            {
                temp2+=str.charAt(j);

            }
             if(h2.containsKey(temp1) && h2.containsKey(temp2))
            { 
                ret+=h2.get(temp1);
                ret+=h2.get(temp2);
            }
            else
            {  
            	dropped = true;
            	break;
          
            }
        }

      if(dropped)
    	  return "-";
      else
        return ret;
 }
    
    ///CRC
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
    
    
    boolean ZeroCheck(String a)
    {
        for(int i = 0; i<a.length(); i++)
        {
            if(a.charAt(i) != '0')
                return false;
        }
        return true;
    }
    boolean drop ;
    
    String CRC(String str)
    {
    	//str = Insert_error(str);
    	
    	String rem = divide(str, divisor);
    	
    	if(ZeroCheck(rem))
    	{
    		drop = false;
    		str = str.substring(0, str.length()-32);
    	}
    	else
    	{
    		drop = true;
    		str = "-";
    	}
    	
    	return str;
    	
    }
   
}