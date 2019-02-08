package frc.robot;

import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
//import edu.wpi.first.wpilibj.hal.I2CJNI;
import edu.wpi.first.hal.I2CJNI;

public class LIDARLite implements PIDSource {

	private static final byte k_deviceAddress = 0x62;

	private final byte m_port;

	private final ByteBuffer m_buffer = ByteBuffer.allocateDirect(2);

	public LIDARLite(Port port) {
		m_port = (byte) port.value;
		System.out.println("RCVI: LIDARLite(): Calling i2CInitialize");
		I2CJNI.i2CInitialize(m_port);
	}

	public void startMeasuring() {
		// Set ACQ_CONFIG_REG (acquisition mode control) register (0x04) to 
		// 1. Disable measurement quick termination
		// 2. Use delay from MEASURE_DELAY (0x45) for burst and free running mode
		//    (initial value of MEASURE_DELAY is 0x14)
		writeRegister(0x04, 0x08 | 32); // default plus bit 5

		// Set OUTER_LOOP_COUNT (burst measurement and count control) register (0x11) 
		// to 0xff (indefinite repetitions after initial distance measurement 
		// command). (Manual also says to see ACQ_CONFIG_REG (0x04) and MEASURE_DELAY 
		// (0x45) for non default automatic repetition delays.)
		writeRegister(0x11, 0xff);

		// Set ACQ_COMMAND (device command) register (0x00) to 0x04 (take distance
		// measurement with receiver bias correction)
		writeRegister(0x00, 0x04);
	}

	public void stopMeasuring() {

		// Set OUTER_LOOP_COUNT (burst measurement and count control) register (0x11) 
		// to 0x00 (one measurement per distance measurement command). This value of
		// 0x00 is equivalent to the default value of 0x01 which also means "one
		// measurement per distance measurement command"
		// (See startMeasuring() for another write to register 0x11 more information.)
		writeRegister(0x11, 0x00);
	}

	public int getDistance() {
		//return readShort(0x8f);
		
		
		// Value in register is actually an unsigned short, but Java has only signed 
		// shorts to read it into. This will make values in the upper half of the 
		// unsigned short range look like negative numbers in the signed short
		Short signedShort = readShort(0x8f);    

		// The full range of the unsigned short can be stored in Java's (signed) 
		// int return value, but must convert it properly using toUnsignedInt; 
		// otherwise, values that were in the upper negative half of the short range 
		// will stay negative in the int return value. 
		return Short.toUnsignedInt(signedShort);  
		
	}

	public int rlreadRegister(int address) {
        Byte signedByte = rlreadByte(address);
		return Byte.toUnsignedInt(signedByte);
	}

	private int writeRegister(int address, int value) {
		m_buffer.put(0, (byte) address);
		m_buffer.put(1, (byte) value);

		return I2CJNI.i2CWrite(m_port, k_deviceAddress, m_buffer, (byte) 2);
	}

	public short readShort(int address) {
		m_buffer.put(0, (byte) address);
		// To do a read, need to first write the address of register we are
		// reading from
		int status = I2CJNI.i2CWrite(m_port, k_deviceAddress, m_buffer, (byte) 1);
		System.out.println("readShort(): i2CWrite(): status: " + status);
		// Now read a short from the register
		status = I2CJNI.i2CRead(m_port, k_deviceAddress, m_buffer, (byte) 2);
		System.out.println("readShort(): i2CRead(): status: " + status);
		return m_buffer.getShort(0);
	}

	private byte rlreadByte(int address) {
		//m_buffer.put(0, (byte) address);
		m_buffer.put(0, (byte) address);
		// To do a read, need to first write the address of register we are
		// reading from
		int status = I2CJNI.i2CWrite(m_port, k_deviceAddress, m_buffer, (byte) 1);
		System.out.println("rlreadByte(): i2CWrite(): status: " + status);
		// Now read a byte from the register
		status = I2CJNI.i2CRead(m_port, k_deviceAddress, m_buffer, (byte) 1);
		System.out.println("rlreadByte(): i2CRead(): status: " + status);
		return m_buffer.get(0);
	}


	@Override
	public void setPIDSourceType(PIDSourceType pidSource) {
		if (pidSource != PIDSourceType.kDisplacement) {
			throw new IllegalArgumentException("Only displacement is supported");
		}
	}

	@Override
	public PIDSourceType getPIDSourceType() {
		return PIDSourceType.kDisplacement;
	}

	@Override
	public double pidGet() {
		return getDistance();
	}
};