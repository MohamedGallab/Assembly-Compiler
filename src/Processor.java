import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class Processor {
	Short[] instructionMemory = new Short[1024];
	byte[] dataMemory = new byte[2048];
	byte[] registers = new byte[64];
	byte SREG;
	short PC = 0;
	int cycles = 1;

	public void parse(String pathToFile) throws IOException {
		int parserIterator = 0;

		File file = new File(pathToFile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		String currentLine[];

		while ((st = br.readLine()) != null) {
			instructionMemory[parserIterator] = 0;
			currentLine = st.split(" ");
			switch (currentLine[0]) {
			case "Add":
				instructionMemory[parserIterator] = 0 << 12;
				break;
			case "SUB":
				instructionMemory[parserIterator] = 1 << 12;
				break;
			case "MUL":
				instructionMemory[parserIterator] = 2 << 12;
				break;
			case "LDI":
				instructionMemory[parserIterator] = 3 << 12;
				break;
			case "BEQZ":
				instructionMemory[parserIterator] = 4 << 12;
				break;
			case "AND":
				instructionMemory[parserIterator] = 5 << 12;
				break;
			case "OR":
				instructionMemory[parserIterator] = 6 << 12;
				break;
			case "JR":
				instructionMemory[parserIterator] = 7 << 12;
				break;
			case "SLC":
				instructionMemory[parserIterator] = (short) (8 << 12);
				break;
			case "SRC":
				instructionMemory[parserIterator] = (short) (9 << 12);
				break;
			case "LB":
				instructionMemory[parserIterator] = (short) (10 << 12);
				break;
			case "SB":
				instructionMemory[parserIterator] = (short) (11 << 12);
				break;
			}
			instructionMemory[parserIterator] = (short) (instructionMemory[parserIterator]
					+ ((Integer.parseInt(currentLine[1]) & 0x3f) << 6) + (Integer.parseInt(currentLine[2]) & 0x3f));
			parserIterator++;
		}
	}

	public Short fetch() {
		return instructionMemory[PC++];
	}

	public int[] decode(short instruction) {
		int[] decodedInstruction = new int[7];

		decodedInstruction[0] = (instruction & 0b1111000000000000) >> 12;	// opcode			
		decodedInstruction[1] = registers[(instruction & 0b0000111111000000) >> 6];	// r1 value
		decodedInstruction[2] = registers[instruction & 0b0000000000111111];	// r2 value
		decodedInstruction[3] = (short) (instruction & 0b0000000000111111);	// imm
		decodedInstruction[4] = (short) (instruction & 0b0000000000111111);	// signedImmediate
		if(((instruction & 32) >> 5) == 1)
			decodedInstruction[4] = (short) ((instruction & 0b0000000000111111) - 64);
		decodedInstruction[5] = (instruction & 0b0000111111000000) >> 6;	// r1 address
		decodedInstruction[6] = instruction & 0b0000000000111111;			// r2 address
		

		return decodedInstruction;
	}

	public boolean execute(int[] decodedInstruction) {
		SREG = 0;
		byte R1 = (byte) decodedInstruction[1];
		byte R2 = (byte) decodedInstruction[2];
		int UnSignedIMM = decodedInstruction[3];
		int SignedIMM = decodedInstruction[4];
		int R1Address = decodedInstruction[5];
		int R2Address = decodedInstruction[6];
		
		
		byte result = 0;
		String oldR1 = registers[R1Address] + "";
		String oldSREG = Integer.toBinaryString(SREG);
		System.out.println("------------------------------------------");
		// 7 6 5 4 3 2 1 0
		// 0 0 0 C V N S Z
		switch (decodedInstruction[0]) {

		// ADD
		case 0:
			result = (byte) (R1 + R2);
			
			// C
			if (((Byte.toUnsignedInt(R1) + Byte.toUnsignedInt(R2))&0b100000000) == 0b100000000) {
				SREG |= (1 << 4);
			}
			// V
			if (R1 >= 0 && R2 >= 0) {
				if ((byte) (result) < 0)
					SREG |= (1 << 3);
			}
			else if (R1 < 0 && R2 < 0) {
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
			registers[R1Address] = result;
			break;

		// SUB
		case 1:
			result = (byte) (R1 - R2);
			
			// V
			if (R1 >= 0 && R2 < 0) {
				if ((byte) (result) < 0)
					SREG |= (1 << 3);
			}
			else if (R1 < 0 && R2 >= 0) {
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
			registers[R1Address] = result;
			break;

		// MUL
		case 2:
			result = (byte) (R1 * R2);
			
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			registers[R1Address] = result;
			break;

		// LDI
		case 3:
			registers[R1Address] = (byte) SignedIMM;
			break;

		// BEQZ
		case 4:
			if (R1 == 0) {
				System.out.println("Old PC value: " + PC);
				PC += 1 + SignedIMM;
				System.out.println("New PC value: " + PC);

			}
			System.out.println("------------------------------------------");
			return true;

		// AND
		case 5:
			result = (byte) (R1 & R2);
			
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			registers[R1Address] = result;
			break;

		// OR
		case 6:
			result = (byte) (R1 | R2);
			
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			registers[R1Address] = result;
			break;

		// JR
		case 7:
			System.out.println("Old PC value: " + PC);
			PC = (short) (R1 << 8 + R2);
			System.out.println("New PC value: " + PC);
			System.out.println("------------------------------------------");
			return true;

		// SLC
		case 8:
			result = (byte) (R1 << UnSignedIMM | R1 >>> 8 - UnSignedIMM);
			
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			registers[R1Address] = result;
			break;

		// SRC
		case 9:
			result = (byte) (R1 >>> UnSignedIMM | R1 << 8 - UnSignedIMM);
			
			// N
			if (result < 0)
				SREG |= (1 << 2);
			// Z
			if (result == 0)
				SREG |= 1;
			registers[R1Address] = result;
			break;

		// LB
		case 10:
			registers[R1Address] = dataMemory[UnSignedIMM];
			break;

		// SB
		case 11:
			System.out.println("Old Memory Cell " + UnSignedIMM + " value: " + dataMemory[UnSignedIMM]);
			dataMemory[UnSignedIMM] = R1;
			System.out.println("New Memory Cell " + UnSignedIMM + " value: " + dataMemory[UnSignedIMM]);
			System.out.println("------------------------------------------");
			return false;
		}
		System.out.println("Old R1 value: " + oldR1);
		System.out.println("Old SREG value: " + oldSREG);
		System.out.println("New R1 value: " + registers[R1Address]);
		System.out.println("New SREG value: " + Integer.toBinaryString(SREG));
		System.out.println("------------------------------------------");
		return false;
	}

	public void run() {
		Short fetchedInstruction = null;
		int[] decodedInstruction = null;
		boolean isJumped = false;
		for (Short instruction : instructionMemory) {

			if (instruction == null && fetchedInstruction == null && decodedInstruction == null) {
				return;
			}
			System.out.println("clock: " + cycles++);
			System.out.println();
			System.out.println("Execute Stage:");
			System.out.println("	" + "Input: " + Arrays.toString(decodedInstruction));
			if (decodedInstruction != null) {
				System.out.println("	" + "Output: ");
				isJumped = execute(decodedInstruction);
				decodedInstruction = null;
			}

			System.out.println();
			System.out.println("Decode Stage:");
			System.out.println("	" + "Input: " + fetchedInstruction);
			if (fetchedInstruction != null) {
				decodedInstruction = decode(fetchedInstruction);
				fetchedInstruction = null;
			}
			System.out.println("	" + "Output: " + Arrays.toString(decodedInstruction));
			System.out.println();
			System.out.println("Fetch Stage:");
			System.out.println("	" + "Input: " + PC);
			fetchedInstruction = fetch();
			System.out.println("	" + "Output: " + fetchedInstruction);

			System.out.println();

			System.out.println("Registers 		: " + Arrays.toString(registers));
			System.out.println("Data Memory		: " + Arrays.toString(dataMemory));
			System.out.println("Instruction Memory	: " + Arrays.toString(instructionMemory));

			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			if(isJumped)
			{
				fetchedInstruction = null;
				decodedInstruction = null;
			}
		}

	}

	public int getBit(byte b, int position) {
		return (b >> position) & 1;
	}

	public static void main(String[] args) {

		Processor processor = new Processor();
		try {
			processor.parse("test2.txt");
		}
		catch (IOException e) {
			System.out.println("Wrong path");
		}
		processor.run();
		
		

//		for (String x : processor.instructionMemory)
//			if (x != null) {
//				System.out.println(x.substring(0, 4) + " | " + x.substring(4, 10) + " | " + x.substring(10, 16));
//				System.out.println(x);
//			}

	}
}
