package assign1;

/**
 * Assignment 1
 * Submitted by:
 * Student Barak Bitan
 */
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import base.compressor;

public class HufmannEncoderDecoder implements compressor {
	public HufmannEncoderDecoder() {
		//Not used
	}
	@Override
	public void Compress(String[] input_names, String[] output_names) {
		byte[] compressedFile=CompressWithArray(input_names,output_names);
	}

	@Override
	public void Decompress(String[] input_names, String[] output_names) {
		byte[] decompressedFile=DecompressWithArray(input_names,output_names);
	}

	@Override
	public byte[] CompressWithArray(String[] input_names, String[] output_names) {
		// Insert the file into an array of bytes
		byte[] toSend=null;
		for (int i = 0; i < input_names.length; i++) {
			Path path = Paths.get(input_names[i]);
			try {
				byte[] firstBytesFile = Files.readAllBytes(path);
				// Find frequencies in a file when each index
				// marks the ASCII representation of the character
				// The number of occurrences of a character is its content in
				// the array
				int[] frequency = new int[256];
				for (int j = 0; j < firstBytesFile.length; j++) {
					frequency[(firstBytesFile[j]) & (0xFF)]++;
				}

				// Create the PriorityQueue node
				PriorityQueue<Node> minHeap = new PriorityQueue<Node>();
				for (int j = 0; j < frequency.length; j++) {
					if (frequency[j] != 0) {
						Node node = new Node((byte) (j & (0xFF)), frequency[j]);
						minHeap.add(node);
					}
				}
				// Create the tree
				while (minHeap.size() > 1) {
					Node node = new Node(minHeap.poll(), minHeap.poll());
					minHeap.add(node);
				}
				Node head =  minHeap.poll();

				// Add bits to node for a new representation
				// Creates String arr to show each character its encoding
				Node[] charToBit = new Node[256];
				addBitsToVertex(head,charToBit,"");
				BitSet bitSet = new BitSet();
				int stop=0;
				int indexBitSet=0;
				for (int j = 0; j < firstBytesFile.length; j++) {
					stop=0;
					//taking the representation of each symbol from node array and inserting it into bit set
					while(stop<charToBit[firstBytesFile[j] & (0xFF)].bitRep.size()){
						if(charToBit[firstBytesFile[j] & (0xFF)].bitRep.get(stop)==true)
							bitSet.set(indexBitSet);
						indexBitSet++;
						stop++;}
				}
				//file to send
				toSend=bitSet.toByteArray();
				//frequency to send as String
				FileOutputStream fileOut=new FileOutputStream(output_names[i]);
				String key="";
				for (int j = 0; j < frequency.length; j++) {
					key+=frequency[j] + ",";
				}
				key+="*";
				Files.write(Paths.get(output_names[i]), key.getBytes());
				Files.write(Paths.get(output_names[i]), toSend,StandardOpenOption.APPEND);
				fileOut.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return toSend ;
	}

	@Override
	public byte[] DecompressWithArray(String[] input_names,	String[] output_names) {
// TODO Auto-generated method stub
		byte[] arrToSend=null;
		for(int i=0;i<input_names.length;i++){
			Path path = Paths.get(input_names[i]);
			try {
				byte[] CompressBytesFile = Files.readAllBytes(path);
				int fileIndex=0;
				int[] frecDec =new int[256];
				int index = 0;
				//Running till' the end of the frequency arr in the compressed file
				while(fileIndex<CompressBytesFile.length&&CompressBytesFile[fileIndex]!='*')
				{
					String freq = "";
					//reading each frequency and inserting it to array for future use 
					while(CompressBytesFile[fileIndex] != ','){
						freq+=(char)CompressBytesFile[fileIndex];
						fileIndex++;
					}
					int temp = Integer.parseInt(freq);
					frecDec[index]=temp;
					index++;
					fileIndex++;
				}
				fileIndex++;
				//building priority Queue as in the compress
				PriorityQueue<Node> minHeap = new PriorityQueue<Node>();
				for (int j = 0; j < frecDec.length; j++) {
					if (frecDec[j] != 0) {
						Node node = new Node((byte)( j & (0xFF)),frecDec[j]);
						minHeap.add(node);
					}
				}

				// Create the tree
				while (minHeap.size() > 1) {
					Node node = new Node(minHeap.poll(), minHeap.poll());
					minHeap.add(node);
				}
				// Add bits to node for a new representation
				// Creates String arr to show each character its encoding
				Node node =  minHeap.poll();
				Node[] charToBit = new Node[256];
				addBitsToVertex(node,charToBit,"");
				byte[] compressedText=new byte[CompressBytesFile.length-fileIndex];
				for (int j = 0; j < compressedText.length; j++) {
					compressedText[j]=CompressBytesFile[fileIndex];
					fileIndex++;
				}
				BitSet CompressedBit=BitSet.valueOf(compressedText);//make byte array into bit set
				LinkedList<Byte> findByteToSend = new LinkedList<Byte>();
				int done=0;
				Node current=null;    //root;
				int bitSetIndex=0;
				while(done<CompressedBit.length()){
					current=node;
					//searching in the tree the character that match the representation
					while(current.right!=null&&current.left!=null){
						if(CompressedBit.get(bitSetIndex)==true)
							current=current.right;
						else
							current=current.left;
						bitSetIndex++;
						done++;
					}
					//adding the character to the linked list
					findByteToSend.add(current.valueIndex);
				}
				arrToSend = new byte[findByteToSend.size()];
				//converting the linked list to byte array
				for(int j=0;j<findByteToSend.size();j++)
				{
					arrToSend[j]=findByteToSend.get(j);
				}
				Files.write(Paths.get(output_names[i]),  arrToSend );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();d
			}
		}		return arrToSend;
	}

	public void addBitsToVertex(Node node, Node[] charToBit,String representation) {
		if (node.right == null && node.left == null) {
			for(int j=0;j<representation.length();j++){
				if(representation.charAt(j)=='0')
					node.bitRep.add(false);
					else
						node.bitRep.add(true);
			}
			charToBit[(int) node.valueIndex& (0xFF)] = node;
			return;
		} else {
			addBitsToVertex(node.left, charToBit,representation+"0");
			addBitsToVertex(node.right, charToBit,representation+"1");
		}
	}
	/*public void printTree(Node root)
	{
		if(root.right==null&&root.left==null){
			System.out.print((char)root.valueIndex);
			for(int j=0;j<root.bitRep.size();j++)
			System.out.print(" "+root.bitRep.get(j));
			System.out.println();
		}
		else {

			printTree(root.left);
			printTree(root.right);
		}

	}*/
	class Node implements Comparable<Node>
	/*
	 * the implements for that the minimum shall be according to the priority
	 * with the func compareTo
	 */
	{
		Node right = null;
		Node left = null;
		int dataFreq = 0;
		byte valueIndex = 0;
		ArrayList<Boolean> bitRep = new ArrayList<Boolean>();

		public Node(byte valueIndex, int dataFreq) {
			this.valueIndex = valueIndex;
			this.dataFreq = dataFreq;
		}

		public Node(Node a, Node b) {
			this.dataFreq = a.dataFreq + b.dataFreq;
			this.left = a;
			this.right = b;
		}

		public int compareTo(Node comperNodes) {
			return this.dataFreq - comperNodes.dataFreq;
		}
	}
}
