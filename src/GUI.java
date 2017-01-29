import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JComboBox;

public class GUI {

	static String ErrorMode = "", Protocol = "", BlockCoding = "", Encoding = ""; 
	public File file;
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		frame.setBounds(100, 100, 750, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblDataTransferer = new JLabel("Simple File Transfer System");
		lblDataTransferer.setBounds(252, 38, 239, 37);
		frame.getContentPane().add(lblDataTransferer);
		
		JLabel lblChooseAFile = new JLabel("Choose a file:");
		lblChooseAFile.setBounds(49, 113, 168, 37);
		frame.getContentPane().add(lblChooseAFile);
		
		final JButton btnUploadFile = new JButton("Upload File");
		btnUploadFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setBounds(245, 12, 517, 326);
				frame.getContentPane().add(fileChooser);
				int retval=fileChooser.showOpenDialog(btnUploadFile);
                if (retval == JFileChooser.APPROVE_OPTION) {
                    //... The user selected a file, get it, use it.
                    file = fileChooser.getSelectedFile();
                    System.out.println(file.getPath());
                }
			}
		});
		btnUploadFile.setBounds(522, 119, 117, 25);
		frame.getContentPane().add(btnUploadFile);
		
		JLabel lblChooseErrorDetection = new JLabel("Choose Error Detection and Correction Mode:");
		lblChooseErrorDetection.setBounds(49, 174, 453, 15);
		frame.getContentPane().add(lblChooseErrorDetection);
		
		String[] ErrorTypes = {"CRC-32", "Hamming distance"};
		JComboBox ErrorDropDown = new JComboBox(ErrorTypes);
		ErrorDropDown.setBounds(522, 169, 216, 25);
		frame.getContentPane().add(ErrorDropDown);
		ErrorDropDown.addActionListener(new ActionListener() {
			 
		    @Override
		    public void actionPerformed(ActionEvent event) {
		        JComboBox<String> combo = (JComboBox<String>) event.getSource();
		        ErrorMode = (String) combo.getSelectedItem().toString();
		        System.out.println(ErrorMode);
		    }

		    
		});

		
		JLabel lblChooseProtocolType = new JLabel("Choose Protocol type:");
		lblChooseProtocolType.setBounds(49, 234, 309, 15);
		frame.getContentPane().add(lblChooseProtocolType);
		
		String[] Protocols = {"GO-Back-N ARQ", "Selective Repeat"};
		JComboBox ProtDropDown= new JComboBox(Protocols);
		ProtDropDown.setBounds(522, 229, 216, 24);
		frame.getContentPane().add(ProtDropDown);
		
		ProtDropDown.addActionListener(new ActionListener() {
			 
		    @Override
		    public void actionPerformed(ActionEvent event) {
		        JComboBox<String> combo = (JComboBox<String>) event.getSource();
		       Protocol = (String) combo.getSelectedItem().toString();
		   
		    }

		    
		});
		
		JLabel lblChooseBlockCoding = new JLabel("Choose Block Coding scheme:");
		lblChooseBlockCoding.setBounds(49, 277, 347, 37);
		frame.getContentPane().add(lblChooseBlockCoding);
		
		String[] Block = {"4B/5B"};
		JComboBox BlockCodeDD = new JComboBox(Block);
		BlockCodeDD.setBounds(522, 283, 216, 24);
		frame.getContentPane().add(BlockCodeDD);
		
		BlockCodeDD.addActionListener(new ActionListener() {
			 
		    @Override
		    public void actionPerformed(ActionEvent event) {
		        JComboBox<String> combo = (JComboBox<String>) event.getSource();
		        BlockCoding = (String) combo.getSelectedItem().toString();
		   
		    }

		    
		});
		
		JLabel lblChooseEncodingScheme = new JLabel("Choose Encoding Scheme:");
		lblChooseEncodingScheme.setBounds(49, 350, 347, 15);
		frame.getContentPane().add(lblChooseEncodingScheme);
		
		String[] Encode = {"NRZ-L", "NRZ-I", "RZ", "Manchester", "Differential Manchester"};
		JComboBox EncodeDD= new JComboBox(Encode);
		EncodeDD.setBounds(522, 345, 216, 24);
		frame.getContentPane().add(EncodeDD);
		
		EncodeDD.addActionListener(new ActionListener() {
			 
		    @Override
		    public void actionPerformed(ActionEvent event) {
		        JComboBox<String> combo = (JComboBox<String>) event.getSource();
		        Encoding= (String) combo.getSelectedItem().toString();
		   
		    }

		    
		});
		
		JButton btnNewButton = new JButton("Send");
	
		btnNewButton.setBounds(149, 464, 117, 25);
		frame.getContentPane().add(btnNewButton);
		
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if(Protocol.equals("GO-Back-N ARQ"))
						new Sender_GBN(file);
					else
						new Sender_SR(file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		
		JButton btnReceive = new JButton("Receive");
		btnReceive.setBounds(484, 464, 117, 25);
		frame.getContentPane().add(btnReceive);
		
		btnReceive.addMouseListener(new MouseAdapter() {
			//@Override
		public void mouseClicked(MouseEvent e) {
				try {
					if(Protocol.equals("GO-Back-N ARQ"))
						new Receiver_GBN();
					else
						new Receiver_SR();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		
		
	
}
}