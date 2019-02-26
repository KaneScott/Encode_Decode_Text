import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Encode {
	private HashMap<Integer, String> dictionary = new HashMap<Integer, String>();
	private HashMap<String, Integer> valMap = new HashMap<String, Integer>();
	private int count = 1;
	public Node root = new Node(null, 0);
	public Node decodeRoot = new Node(null, 0);

	public class Node implements Comparable<Integer> {
		private String value;
		private Integer index;
		private Node parent = null;
		private ArrayList<Node> children;
		private boolean isLeaf = true;

		public Node(String v, Integer i) {
			value = v;
			index = i;
			children = new ArrayList<Node>();
		}

		public ArrayList<Node> getChildren() {
			return children;
		}

		private void setParent(Node p) {
			parent = p;
		}

		public Node getParent() {
			return parent;
		}

		public void addChild(Node c, int currIndex) {
			if (children.size() == 0) {
				children.add(c);
				isLeaf = false;
				return;
			} else {
				if (children.contains(c)) {
					return;
				} else if (c.getValue().length() == currIndex) {
					children.add(c);
					c.setParent(this);
					return;
				}
				for (Node child : children) {
					if (child.getValue().equals(c.getValue().substring(0, currIndex))) {
						c.setParent(child);
						child.addChild(c, currIndex + 1);

						return;
					}
				}
			}
		}

		public void print(int index) {
			String tab = "\t";
			tab = String.join("", Collections.nCopies(index, tab));
			for (Node child : children) {
				System.out.println(tab + "<" + child.getIndex() + "," + child.getValue() + ">");
				child.print(index + 1);
			}
		}

		public void removeChild(Node c) {
			children.remove(c);
		}

		public String getValue() {
			return value;
		}

		public Integer getIndex() {
			return index;
		}

		public boolean isLeaf() {
			return isLeaf;
		}

		@Override
		public int compareTo(Integer o) {
			return Integer.compare(index, o);
		}

	}

	public String binarizer(String args) {
		byte[] bytes = args.getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes) {
			int val = b;
			for (int i = 0; i < 8; i++) {
				binary.append((val & 128) == 0 ? 0 : 1);
				val <<= 1;
			}
		}
		return binary.toString();
	}

	String toBinary(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
		for (int i = 0; i < Byte.SIZE * bytes.length; i++)
			sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
		return sb.toString();
	}

	String toBinarySingle(byte b) {
		StringBuilder sb = new StringBuilder(Byte.SIZE);
		for (int i = 0; i < Byte.SIZE; i++)
			sb.append((b << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
		return sb.toString();
	}

	byte[] fromBinary(String s) {
		int sLen = s.length();
		byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
		char c;
		for (int i = 0; i < sLen; i++)
			if ((c = s.charAt(i)) == '1')
				toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
			else if (c != '0')
				throw new IllegalArgumentException();
		return toReturn;
	}

	private ArrayList<Node> encodedNodeList = new ArrayList<Node>();

	public byte[][] encodeProcess(String args) {
		ArrayList<Node> output = new ArrayList<Node>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(args));
			String line = br.readLine();
			while (line != null) {
				output.addAll(encode(line));
				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		encodedNodeList.addAll(output);
		ArrayList<String> binaryOutput = new ArrayList<String>();
		byte[][] bitPacked = new byte[output.size()][7];
		System.out.println("---------------------------Encoder Output---------------------------");
		int count = 0;
		for (Node out : output) {
			binaryOutput.add(binarizer((out.getIndex() + "," + out.getValue())));
			bitPacked[count] = fromBinary(binaryOutput.get(count));
			System.out.println("<" + out.getIndex() + "," + out.getValue() + ">");
			count++;
		}
		System.out.println("---------------------------Encoder Output---------------------------");

		System.out.println("---------------------------Bit-packer Output---------------------------");
		for (byte[] bit : bitPacked) {
			String s = "";
			for (byte b : bit) {
				s += b + "||";
			}
			System.out.println(s);
		}
		System.out.println("---------------------------Bit-packer Output---------------------------");
		return bitPacked;
	}

	public String decodeProcess(byte[][] bitPacked) {
		ArrayList<String> binaryOutput = new ArrayList<String>();
		ArrayList<String> stringPairs = new ArrayList<String>();
		ArrayList<Node> nodeList = new ArrayList<Node>();
		for (byte[] bit : bitPacked) {
			binaryOutput.add(toBinary(bit));
		}
		for (String bin : binaryOutput) {
			stringPairs.add(debinarizer(bin));
		}
		// ---------------------------De-binarizer
		// Output---------------------------
		for (String sp : stringPairs) {
			String[] split = sp.split(",", 2);
			Integer index = Integer.parseInt(split[0]);
			Node newNode = new Node(split[1], index);
			nodeList.add(newNode);
		}
		// ---------------------------De-binarizer
		// Output---------------------------
		String decoded = decode(nodeList); // Decode String
		System.out.println("Decoded string: " + decoded);
		return decoded;
	}

	public String debinarizer(String args) {
		String decoded = Arrays.stream(args.split("(?<=\\G.{8})")).map(s -> Integer.parseInt(s, 2))
				.map(i -> "" + Character.valueOf((char) i.intValue())).collect(Collectors.joining(""));
		return decoded;
	}

	/*
	 * Returns an arraylist of Nodes. Each node contains the number(index), and
	 * string(mismatched character) associated with the output of encoding text
	 * via LZ78
	 */
	public ArrayList<Node> encode(String args) {
		int size = 1;
		int start = 0;
		int lmi = 0;
		ArrayList<Node> encoded = new ArrayList<Node>();
		for (int i = 0; i < args.length(); i++) {
			if (size == 1)
				start = i;
			String set = args.substring(start, start + size);
			if (dictionary.containsValue(set)) {
				size++;
				lmi = valMap.get(set);
			} else {
				root.addChild(new Node(set, lmi), 1);
				dictionary.put(count, set);
				valMap.put(set, count);
				count++;
				start = 0;
				size = 1;
				encoded.add(new Node(Character.toString(args.charAt(i)), lmi));
				lmi = 0;
			}
		}
		System.out.println(args);
		return encoded;
	}

	public void recursAdd(Node root, Node nc, int index) {
		for (Node child : root.getChildren()) {
			if (child.getIndex() == nc.getIndex()) {
				child.addChild(new Node(child.getValue().concat(nc.getValue()), index), 1);
				return;
			} else if (child.getChildren().size() > 0) {
				for (Node cc : child.getChildren()) {
					recursAdd(cc, nc, index);
				}
			}
		}
	}

	public String decode(ArrayList<Node> nl) {
		int index = 1;
		ArrayList<Node> dec = new ArrayList<Node>();
		System.out.println("-----------------Decoding dictionary-----------------");
		for (Node n : nl) {
			if (n.getIndex() == -1) {
				break;
			}
			if (n.getIndex() == 0) {
				dec.add(new Node(n.getValue(), index));
			} else {
				Node found = null;
				for (Node dictN : dec) {
					if (dictN.getIndex().equals(n.getIndex())) {
						found = new Node(dictN.getValue().concat(n.getValue()), index);
						System.out.println("\t(" + n.getIndex() + "," + n.getValue() + "> <" + found.getIndex() + ","
								+ found.getValue() + ">");
						break;
					}
				}
				if (found != null) {
					dec.add(found);
				}
			}
			index++;
		}
		String decoded = "";
		for (Node n : dec) {
			if (n != null) {
				System.out.println("<" + n.getIndex() + "," + n.getValue() + ">");
				decoded += n.getValue();
			}
		}
		System.out.println("-----------------Decoding dictionary-----------------");
		return decoded;
	}

}