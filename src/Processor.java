import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Processor {
	String[] instructionMemory = new String[1024];
	String[] dataMemory = new String[2048];
	byte[] registers = new byte[64];
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
					+ extend(Integer.toBinaryString(Integer.parseInt(currentLine[1]) & 0x3f), 6)
					+ extend(Integer.toBinaryString(Integer.parseInt(currentLine[2]) & 0x3f), 6);
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
		SREG = 0;
		int R1 = decodedInstruction[1];
		int R2 = decodedInstruction[2];
		byte result = 0;
		// 7 6 5 4 3 2 1 0
		// 0 0 0 C V N S Z
		switch (decodedInstruction[0]) {

		// ADD
		case 0:
			result = (byte) (registers[R1] + registers[R2]);
			registers[R1] = result;
			// C
			if (Byte.toUnsignedInt(registers[R1]) + Byte.toUnsignedInt(registers[R2]) > Byte.MAX_VALUE) {
				SREG |= (1 << 4);
			}
			// V
			if (registers[R1] >= 0 && registers[R2] >= 0) {
				if ((byte) (result) < 0)
					SREG |= (1 << 3);
			}
			else if (registers[R1] < 0 && registers[R2] < 0) {
				if ((byte) (result) >= 0)
					SREG |= (1 << 3);
			}
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// S
			SREG |= (((SREG >> 2) & 1) ^ ((SREG >> 3) & 1)) << 1;
			// Z
			if (result == 0)
				SREG |= 1;
			break;

		// SUB
		case 1:
			result = (byte) (registers[R1] - registers[R2]);
			registers[R1] = result;
			// V
			if (registers[R1] >= 0 && registers[R2] < 0) {
				if ((byte) (result) < 0)
					SREG |= (1 << 3);
			}
			else if (registers[R1] < 0 && registers[R2] >= 0) {
				if ((byte) (result) >= 0)
					SREG |= (1 << 3);
			}
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// S
			SREG |= (((SREG >> 2) & 1) ^ ((SREG >> 3) & 1)) << 1;
			// Z
			if (result == 0)
				SREG |= 1;
			break;

		// MUL
		case 2:
			result = (byte) (registers[R1] * registers[R2]);
			registers[R1] = result;
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			break;

		// LDI
		case 3:
			registers[R1] = (byte) R2;
			break;

		// BEQZ
		case 4:
			if (registers[R1] == 0)
				PC += 1 + R2;
			break;

		// AND
		case 5:
			result = (byte) (registers[R1] & registers[R2]);
			registers[R1] = result;
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			break;

		// OR
		case 6:
			result = (byte) (registers[R1] | registers[R2]);
			registers[R1] = result;
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			break;

		// JR
		case 7:
			PC = (short) (registers[R1] << 8 + registers[R2]);
			break;

		// SLC
		case 8:
			result = (byte) (registers[R1] << R2 | registers[R1] >>> 8 - R2);
			registers[R1] = result;
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			break;

		// SRC
		case 9:
			result = (byte) (registers[R1] >>> R2 | registers[R1] << 8 - R2);
			registers[R1] = result;
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			break;

		// LB
		case 10:
			registers[R1] = (byte) Integer.parseUnsignedInt(dataMemory[R2], 2);
			break;

		// SB
		case 11:
			dataMemory[R2] = Integer.toBinaryString(registers[R1] & 0xff);
			break;
		}

	}

	public void run() {
		String fetchedInstruction = null;
		int[] decodedInstruction = null;
		for (String instruction : instructionMemory) {
			System.out.println(cycles++);
			System.out.println("	" + instruction);
			System.out.println("	" + fetchedInstruction);
			System.out.println("	" + decodedInstruction);
			System.out.println();
			if (instruction == null && fetchedInstruction == null && decodedInstruction == null)
				return;
			if (decodedInstruction != null)
				execute(decodedInstruction);
			if (fetchedInstruction != null)
				decodedInstruction = decode(fetchedInstruction);
			if(fetchedInstruction == null)
				decodedInstruction =null;
			fetchedInstruction = fetch();
			// memory ( ) ;
			// writeback ( ) ;
		}
	}

	public int getBit(byte b, int position) {
		return (b >> position) & 1;
	}

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
		processor.run();
//		for (String x : processor.instructionMemory)
//			if (x != null) {
//				System.out.println(x.substring(0, 4) + " | " + x.substring(4, 10) + " | " + x.substring(10, 16));
//				System.out.println(x);
//			}

//		Byte x = (byte) Integer.parseUnsignedInt("10000000",2);
//		String y = "11110000";
//		x = (byte)Integer.parseUnsignedInt(y, 2);
//		byte y = 2;
//		System.out.println(Integer.toBinaryString(y&0xff));
//		
//		String x = "-5";
//		String y = "5";
//		System.out.println(processor.extend(Integer.toBinaryString(Integer.parseInt(x)&0xff), 8));
//		System.out.println(processor.extend(Integer.toBinaryString(Integer.parseInt(y)&0xff), 8));

//		byte x = (byte) (128 + 2);
//		System.out.println(x < 0);

	}
}
