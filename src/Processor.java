import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Processor {
	String[] instructionMemory = new String[1024];
	String[] dataMemory = new String[2048];
	String[] registers = new String[64];
	byte SREG;
	short PC = 0;
	int cycles = 1;

	public void parse(String pathToFile) throws IOException {
		int parserIterator = 0;
		String result = "";

		File file = new File(pathToFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		String currentLine[];

		while ((st = br.readLine()) != null) {
			currentLine = st.split(" ");
			switch (currentLine[0]) {
			case "Add":
				result = Integer.toBinaryString(0);
				break;

			case "SUB":
				result = Integer.toBinaryString(1);

				break;
			case "MUL":
				result = Integer.toBinaryString(2);

				break;
			case "LDI":
				result = Integer.toBinaryString(3);

				break;
			case "BEQZ":
				result = Integer.toBinaryString(4);

				break;
			case "AND":
				result = Integer.toBinaryString(5);

				break;
			case "OR":
				result = Integer.toBinaryString(6);

				break;
			case "JR":
				result = Integer.toBinaryString(7);

				break;
			case "SLC":
				result = Integer.toBinaryString(8);

				break;
			case "SRC":
				result = Integer.toBinaryString(9);

				break;
			case "LB":
				result = Integer.toBinaryString(10);

				break;
			case "SB":
				result = Integer.toBinaryString(11);

				break;
			}
			instructionMemory[parserIterator] = extend(result, 4)
					+ extend(Integer.toBinaryString(Integer.parseInt(currentLine[1])), 6)
					+ extend(Integer.toBinaryString(Integer.parseInt(currentLine[2])), 6);
			parserIterator++;
		}

	}

	public String extend(String s, int finalSize) {
		while (s.length() < finalSize)
			s = "0" + s;
		return s;
	}

	public String fetch() {
		return instructionMemory[PC++];
	}

	public int[] decode(String instruction) {
		int[] decodedInstruction = new int[3];
		decodedInstruction[0] = Integer.parseInt(instruction.substring(0, 4), 2);
		decodedInstruction[1] = Integer.parseInt(instruction.substring(4, 10), 2);
		decodedInstruction[2] = Integer.parseInt(instruction.substring(10, 16), 2);
		return decodedInstruction;
	}

	public void execute(int[] decodedInstruction) {
		int R1 = Integer.parseInt(registers[1], 2);
		int R2 = Integer.parseInt(registers[2], 2);
		switch (decodedInstruction[0]) {
		case 0:
			registers[1] = ""+ (R1 + R2);
			//C = 1 if UNSIGNED[VALUE1] OP UNSIGNED[VALUE2] > Byte.MAX_VALUE
			if(R1 + R2> Byte.MAX_VALUE)
			{
				SREG |= (1 << 4); 
				//"000CVNSZ"
				//"00010000"
			}
			break;

		default:
			break;
		}

	}

	public void run() {
		String fetchedInstruction = null;
		int[] decodedInstruction = null;
		for (String instruction : instructionMemory) {
			if (instruction == null && fetchedInstruction == null && decodedInstruction == null)
				return;
			if (decodedInstruction != null)
				execute(decodedInstruction);
			if (fetchedInstruction != null)
				decodedInstruction = decode(fetchedInstruction);
			fetchedInstruction = fetch();
			cycles++;
			// memory ( ) ;
			// writeback ( ) ;
		}

	}

//	fetch () ;
//	decode () ;
//	execute () ;
//	// memory () ;
//	// write back () ;
//	cycles ++;

	public static void main(String[] args) {
//	    byte x = -128;
//		System.out.println(Byte.MAX_VALUE);
//		System.out.println(Integer.toBinaryString(x).substring(24));
//		String x = "1000" + "000100" + "001011";
//		int z = Integer.parseInt(x, 2);
//		short y = (short)z;
//		System.out.println(Integer.toBinaryString(z));
//		System.out.println(Integer.toBinaryString(y).substring(16));
//		byte x = -127;
//		System.out.println(((byte)x<<1));
//		"7" --> 7 --> "111"
//		System.out.println(Integer.toBinaryString(Integer.parseInt("7")));

		Processor processor = new Processor();
		try {
			processor.parse("test.txt");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		for (String x : processor.instructionMemory)
			if (x != null) {
				System.out.println(x.substring(0, 4) + " | " + x.substring(4, 10) + " | " + x.substring(10, 16));
			}
//		byte x =10;
//		System.out.println(Byte.);
	}
}
