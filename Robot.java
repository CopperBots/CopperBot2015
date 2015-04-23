package org.usfirst.frc.team2586.robot;

import edu.wpi.first.wpilibj.CameraServer;  
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Gyro;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	Joystick joy = new Joystick(0);
	Joystick xbox = new Joystick(1);
	
	Talon frontRightDrive = new Talon(0);
	Talon rearRightDrive = new Talon(1);
	Talon frontLeftDrive = new Talon(2);
	Talon rearLeftDrive = new Talon(3);
	
	Gyro gyro = new Gyro(0);
	
	Encoder fRDcoder = new Encoder(0, 1);
	Encoder rRDcoder = new Encoder(2, 3);
	Encoder fLDcoder = new Encoder(4, 5);
	Encoder rLDcoder = new Encoder(6, 7);
	
	Timer timer = new Timer();
	
	Solenoid liftUp = new Solenoid(0);
	Solenoid liftDown = new Solenoid(1);
	
	PowerDistributionPanel pdp = new PowerDistributionPanel();
	
	CameraServer server;
	
	Relay lux = new Relay(0);
	Relay openClaw = new Relay (1);
	Relay closeClaw = new Relay (2); 
	
	Servo cameraServoX = new Servo(9);
    Servo cameraServoY = new Servo(8);	
    Servo eyeServo = new Servo(7);
	
	
	
	boolean precisionMode = true;
	boolean gyroMode = true;
	boolean crabMode = false;
	
	double gyroHeading = 0;
	double autoSelect = 7;
    double dangerZone = 0.5;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		//Starts the gyro and camera, along with providing default values for our Autonomous Picker and Gyro kP
		gyro.initGyro();
		SmartDashboard.putNumber("Auton", 7);
		SmartDashboard.putNumber("Gyro_kP", 0.05);
		server = CameraServer.getInstance();
		server.setQuality(50);
		// the camera name (ex "cam0") can be found through the roborio web
		// interface
		server.startAutomaticCapture("cam0");
	//	table = NetworkTable.getTable("database");
		
	}
	@Override
	public void disabledPeriodic() {
		//Lets us see if Precision/Gyro modes are on or off
		double precision = joy.getRawAxis(3);
		
		SmartDashboard.putBoolean("Precision Mode", precisionMode);
		SmartDashboard.putBoolean("Gyro Mode", gyroMode);
		SmartDashboard.putNumber("precision", precision);
		
		if (joy.getRawButton(4)) {
			gyroMode = false;
		}
		if (joy.getRawButton(6)) {
			gyroMode = true;
		}
		
		gyro.reset();
		gyroHeading = gyro.getAngle();
		server = CameraServer.getInstance();
		server.setQuality(50);
		server.startAutomaticCapture("cam0");

	}

	@Override
	public void autonomousInit() {
		//Gets the timer ready for auton
		gyroMode = true;
		timer.reset();
		timer.start();
		gyro.reset();
		gyroHeading = gyro.getAngle();
		fRDcoder.reset();
		rRDcoder.reset();
		fLDcoder.reset();
		rLDcoder.reset();

	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		//the below is a large amount of witchcraft that has not been tested yet
		//((as of now most of it has been tested but it is still largely witchcraft))
		double time = timer.get();
		double fRD = 0, rRD = 0, fLD = 0, rLD = 0;
		double rotation = GyroMod(0);

		//lets our supervisors know we aren't messing around on company time, also passes variables to the smart dashboard
		//but mostly the supervision
		//you won't get away with your slacking ways now, keh from accounting
		SmartDashboard.putNumber("Time", time);
		SmartDashboard.putBoolean("LiftUp", liftUp.get());
		SmartDashboard.putBoolean("LiftDown", liftDown.get());
		autoSelect = SmartDashboard.getNumber("Auton");
		
		
		//this is our lovely "default model", available for five easy installments of all your money and everything you love
		//we pick up the bin and hightail outta town like the sherrif's after our loot
		//riding off into the sunset, the bin and the bot go into the auto zone together
		//still a better love story than blues brothers
		//oh there's also a SICKNASTY 90 degree turn after that with the help of our lovely gyro
		//it kinda works without the gyro but that's not how coolkids do it
		if (autoSelect == 1) {
			SmartDashboard.putString("AutoMode", "BinStackSim2015");
			if (time < 1) {
				liftUp.set(true);
			} else if (timer.get() > 2 && timer.get() < 4) {
				fRD = -0.5;
				rRD = -0.5;
				fLD = 0.5;
				rLD = 0.5;
			}
			else if (timer.get() > 4 && timer.get() < 10){
				if (gyroMode){
				gyroHeading = 90;
				}
				else{
				fRD = -0.325;
				rRD = -0.325;
				fLD = -0.325;
				rLD = -0.325;
				}
			}
			else if (timer.get() > 10){
				fRD = 0;
				rRD = 0;
				fLD = 0;
				rLD = 0;
			}
		}
		
		//we swooce forward right into the autozone. right into there. swoocing right in. the autozone. right there.
		if (autoSelect == 2) {
			SmartDashboard.putString("AutoMode", "with great passion");
				if (time < 2) {
					fRD = 0.25;
					rRD = 0.25;
					fLD = -0.25;
					rLD = -0.25;
			} else if (time > 2) {
				fRD = 0;
				rRD = 0;
				fLD = 0;
				rLD = 0;
			}
		}
		
		//it's like the one above but we go backwards this time like a buncha nerds
		if (autoSelect == 3) {
			SmartDashboard.putString("AutoMode", "aaaaa run away");
				if (time < 2) {
					fRD = -0.25;
					rRD = -0.25;
					fLD = 0.25;
					rLD = 0.25;
			} else if (time > 2) {
				fRD = 0;
				rRD = 0;
				fLD = 0;
				rLD = 0;
			}
		}
		
		//so this has never actually been tested or used or anything. look at that big block of code. never used!
		//ah what a lost hope of our generation it is.
		//anyway
		//what it's supposed to do is lift up the bin, strafe to the crate, set the bin on the crate, pick up the crate, go back
		//into the zone, profit
		//it'd be rad as heck but it is also finnicky as heck so it's probs not happening yo
		if (autoSelect == 4) {
			SmartDashboard.putString("AutoMode", "fancypantsbot");
			if (time < 3) {
				liftUp.set(true);
			}
			else if (time > 5 && time < 6) {
				fRD = -0.45;
				rRD = 0.45;
				fLD = -0.45;
				rLD = 0.45;
			}
			else if (time > 6 && time < 9){
				liftDown.set(true);
			}
			else if (time > 9 && time < 10){
				fRD = -0.25;
				rRD = -0.25;
				fLD = 0.25;
				rLD = 0.25;
			}
			else if (time > 10 && time < 11){
				fRD = 0.25;
				rRD = 0.25;
				fLD = -0.25;
				rLD = -0.25;
			}
			else if (time > 11 && time < 13) {
				liftUp.set(true);
			}
			else if (time > 13 && time < 15) {
				fRD = -0.5;
				rRD = -0.5;
				fLD = 0.5;
				rLD = 0.5;
			}
			else if (time > 15){
				fRD = 0;
				rRD = 0;
				fLD = 0;
				rLD = 0;
			}
		}
		
		//it's like a barrel roll, without the roll. or the barrel. we strafe for a couple seconds.
		//i can't remember why this exists.
		if (autoSelect == 5) {
			SmartDashboard.putString("AutoMode", "DO A SIDE ROLL THING FOX");
			if (gyroMode){
				gyroHeading = 0;
				}
			if (time < 6 && fRDcoder.getDistance() > (-750*30)){
				fRD = -0.45;
				rRD = 0.45;
				fLD = -0.45;
				rLD = 0.45;
			}
			else {
				fRD = 0;
				rRD = 0;
				fLD = 0;
				rLD = 0;
			}
		}
		
		//up! down! liftup! liftdown! it doesn't actually lift down. don't ask me why. ask the strongman.
		if (autoSelect == 6){
			SmartDashboard.putString("AutoMode", "the strongman?");
			if (time < 5) {
				liftUp.set(true);
			}
			else if (time > 5) {
				liftDown.set(true);
			}
		}
		
		//this is the single most difficult of all code to code. I sat here in front of the computer for many lightyears trying to  
		//figure out just how the heck. it is my masterpiece. it does literally nothing. you stand still.
		if (autoSelect == 7) {
			SmartDashboard.putString("AutoMode", "STANDING HERE, I REALIIIIZE");
			fRD = 0;
			rRD = 0;
			fLD = 0;
			rLD = 0;
		}
		
		//we go "nyoom" forward using our encoders this time, not just the timer. hopefully more accurate than
		//that one evil clock jerk i hate
		if (autoSelect == 8) {
			double error = 50 - fRDcoder.getDistance();
			double kP = 0.5;
	        double output = error*kP;
			SmartDashboard.putString("AutoMode", "encode THIS, nerd");
			if (time < 1) {
				liftUp.set(true);
			} else if (time > 1 && time < 8) {
			     if (fRDcoder.getDistance() < (50-dangerZone)){
			           fRD = output;
			           rRD = output;
			           fLD = -output;
			           rLD = -output;
			        }
			        else if (fRDcoder.getDistance() > (50+dangerZone)){
			            fRD = -output;
			            rRD = -output;
						fLD = output;
						rLD = output;
			        }
			}
			else if (time > 8 && time < 10){
				if (gyroMode){
				gyroHeading = 90;
				}
				else{
				fRD = -0.325;
				rRD = -0.325;
				fLD = -0.325;
				rLD = -0.325;
				}
			}
			else if (timer.get() > 10){
				fRD = 0;
				rRD = 0;
				fLD = 0;
				rLD = 0;
			}
		}
		if (autoSelect == 9){
			SmartDashboard.putString("AutoMode", "crab bot 2k15");
			if (timer.get() < 3){
				if (gyroMode){
				gyroHeading = -90;
				}
				else{
				fRD = -0.325;
				rRD = -0.325;
				fLD = -0.325;
				rLD = -0.325;
				}
			}
			else if (timer.get() < 6){
				fRD = -0.25;
				rRD = -0.25;
				fLD = 0.25;
				rLD = 0.25;
			}
		}
		if (autoSelect == 10){
			SmartDashboard.putString("AutoMode", "wise mandible hug");
			if (time < 1){
				closeClaw.set(Relay.Value.kForward);
			}
			else if (time > 1 && time < 2){
				liftUp.set(true);
			}
			else if (time > 2 && time < 8 && fRDcoder.getDistance() > (-3000)){
				liftUp.set(false);
				fRD = -0.25;
				rRD = -0.25;
				fLD = 0.25;
				rLD = 0.25;
			}
			else if (time > 8 && time < 11){
				gyroHeading = -90;
			}
			else if (time > 11 && time < 14) {
				liftDown.set(true);
				fRD = 0;
				rRD = 0;
				fLD = 0;
				rLD = 0;
			}
			else if (time > 14 && time < 15) {
				openClaw.set(Relay.Value.kForward);
			}
		}
		if (autoSelect == 11){
			SmartDashboard.putString("AutoMode", "counter mandible hug");
			if (time < 1){
				closeClaw.set(Relay.Value.kForward);
			}
			else if (time > 1 && time < 2){
				liftUp.set(true);
			}
			else if (time > 2 && time < 8 && fRDcoder.getDistance() > (-3000)){
				liftUp.set(false);
				fRD = -0.25;
				rRD = -0.25;
				fLD = 0.25;
				rLD = 0.25;
			}
			else if (time > 8 && time < 11){
				gyroHeading = -90;
			}
			else if (time > 11 && time < 14) {
				liftDown.set(true);
				fRD = 0;
				rRD = 0;
				fLD = 0;
				rLD = 0;
			}
			else if (time > 14 && time < 15) {
				openClaw.set(Relay.Value.kForward);
			}
		}
		//this right here makes sure our robot doesn't do crazy coolkid sicknasty 360 degree noscope turns every time we want
		//to turn it a little bit to the right
		if (rotation > 0.4){
			rotation = 0.4;
		}
		if (rotation < -0.4){
			rotation = -0.4;
		}
		
		//makes our robot atually do the thing
		//the thing being moving
		frontRightDrive.set(fRD + rotation);
		rearRightDrive.set(rRD + rotation);
		frontLeftDrive.set(fLD + rotation);
		rearLeftDrive.set(rLD + rotation);
		
		SmartDashboard.putNumber("fRDcoder", fRDcoder.getDistance());
		SmartDashboard.putNumber("rRDcoder", rRDcoder.getDistance());
		SmartDashboard.putNumber("fLDcoder", fLDcoder.getDistance());
		SmartDashboard.putNumber("rLDcoder", rLDcoder.getDistance());
	}
	@Override
	public void teleopInit() {
		//recalibrates the gyro!
		gyro.reset();
		gyroHeading = gyro.getAngle();
		gyroMode = false;
		lux.set(Relay.Value.kForward);
		fRDcoder.reset();
		rRDcoder.reset();
		fLDcoder.reset();
		rLDcoder.reset();
	}

	/**
	 * This function is called periodically during operator control and david in just teh berst
	 */
	@Override
	public void teleopPeriodic() {
		//Gets the three axis of our joystick, accounting for the dead zone
		double x = -valueWithDeadzone(joy.getRawAxis(0), .1);
		double y = -valueWithDeadzone(joy.getRawAxis(1), .1);
		double rotation = -valueWithDeadzone(joy.getRawAxis(2), .4);
		double precision = joy.getRawAxis(3);
		
		double xboxRX = xbox.getRawAxis(4);
		double xboxRY = xbox.getRawAxis(5);
        double lTrigger = xbox.getRawAxis(2);
        double rTrigger = xbox.getRawAxis(3);
		
        rotation = GyroMod(rotation);
		SmartDashboard.putNumber("Gyro angle", gyro.getAngle());
		
		SmartDashboard.putNumber("fRDcoder", fRDcoder.getDistance());
		SmartDashboard.putNumber("rRDcoder", rRDcoder.getDistance());
		SmartDashboard.putNumber("fLDcoder", fLDcoder.getDistance());
		SmartDashboard.putNumber("rLDcoder", rLDcoder.getDistance());


		
		//Triggers precision mode on or off, which slows the robot to 1/3 motor output
		/*if (joy.getRawButton(7)) {
			precisionMode = false;
		}
		if (joy.getRawButton(8)) {
			precisionMode = true;
		}*/
		
		//Triggers gyro mode on or off (also resetting it to avoid dangerous funky time)
		if (joy.getRawButton(11)) {
			gyroMode = false;
			gyro.reset();
			gyroHeading = gyro.getAngle();
		}
		if (joy.getRawButton(12)) {
			gyroMode = true;
			gyro.reset();
			gyroHeading = gyro.getAngle();
		}
		
		//LIFTS
		liftUp.set(joy.getRawButton(8) || joy.getRawButton(1));
		//SETS DOWN
		liftDown.set(joy.getRawButton(2) || joy.getRawButton(7));
		
		//sets the values for our four drives
		double fRD = (x + y + rotation);
		double rRD = -(x - y - rotation);
		double fLD = -(-x + y - rotation);
		double rLD = (-x - y + rotation);
		
		//echoes of precision mode still linger on
		if (precisionMode) {
			fRD = fRD * ((precision+1) *0.5);
			rRD = rRD * ((precision+1) *0.5);
			fLD = fLD * ((precision+1) *0.5);
			rLD = rLD * ((precision+1) *0.5);
		}
		
		SmartDashboard.putNumber("precision", precision);
		
		//lets us know what our drives are up to, we're worried about them okay
		SmartDashboard.putNumber("Front Right Drive", fRD);
		SmartDashboard.putNumber("Rear Right Drive", rRD);
		SmartDashboard.putNumber("Front Left Drive", fLD);
		SmartDashboard.putNumber("Rear Left Drive", rLD);
		
		//the drives are the drives now
		frontRightDrive.set(fRD);
		rearRightDrive.set(rRD);
		frontLeftDrive.set(fLD);
		rearLeftDrive.set(rLD);
		
		//lets us know which toggles are toggled, yo
		SmartDashboard.putBoolean("Precision Mode", precisionMode);
		SmartDashboard.putBoolean("Gyro Mode", gyroMode);
		
		//monitors our current usage so we can tut disapprovingly at offenders who do not live up to their full potential
		SmartDashboard.putNumber("PDP 14 Current", pdp.getCurrent(14));
		SmartDashboard.putNumber("PDP 15 Current", pdp.getCurrent(15));
		SmartDashboard.putNumber("PDP 0 Current", pdp.getCurrent(0));
		SmartDashboard.putNumber("PDP 1 Current", pdp.getCurrent(1));
		
		//cameracmammreracaamemra CAMERA        
        //if you hold down LT, you can go updownupdownupdown without straying
        if (lTrigger > 0.6){
            cameraServoX.set(0.45);
        }
        else {	
        	cameraServoX.set(xboxRX/4 + 0.45); 
        }
        

        //if you hold down RT, never ascend, never fall. no enlightenment. no judgement. you are nirvana, camera.
        if (rTrigger > 0.6){
            cameraServoY.set(0.5);
        }
        else {
            cameraServoY.set((-xboxRY/4) +0.5);
        }

		
        eyeServo.set(xboxRX + 0.5);
        
		//the robots cruel crushing maw's mandibles will clench or unclench with great fury and righteous anger, for 
        //they walk in the valley of the shadow of death
		if (joy.getRawButton(9)){
			openClaw.set(Relay.Value.kForward);
		}else{
			openClaw.set(Relay.Value.kOff);
		}
		if (joy.getRawButton(10)){
			closeClaw.set(Relay.Value.kForward);
		}else{
			closeClaw.set(Relay.Value.kOff);
		}
		
		//setProxLights();
	}
	
	
	//aight this cool thing right here will trigger blinky lights if the switches on the front of the bot are triggered
		//public void setProxLights () {
			//if (frontLeftSwitch.get()){
				//frontLeftLight.set(Relay.Value.kOn);
				
			//} else{
				//frontLeftLight.set(Relay.Value.kOff);
			//}
				
			//if (frontRightSwitch.get()){
				//frontRightLight.set(Relay.Value.kOn);
			//}else{
				//frontRightLight.set(Relay.Value.kOff);
				
			//}
				

		//
	
	/**
	 * ' This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {

	}
	
	//super fancy calculations that make the robot better to control because our joystick has a dead zone
	public double valueWithDeadzone(double in, double dead) {
		if (-dead < in && in < dead)
			return 0;
		if (in < 0) {
			return in + dead;
		} else {
			return in - dead;
		}
	}
	//funky fresh gyro beats so our robot stays on the straight and narrow
		public double GyroMod(double rotation){
			if (gyroMode) {
				double error = gyro.getAngle() - gyroHeading;
				double kP = SmartDashboard.getNumber("Gyro_kP", .05);
				if (rotation == 0) {
					rotation = rotation + kP * error;
				} else {
					gyroHeading = gyro.getAngle();
				}
				SmartDashboard.putNumber("Gyro Heading", gyroHeading);
				SmartDashboard.putNumber("Gyro Error", error);
			}
			SmartDashboard.putNumber("Rotation", rotation);
			return rotation;
		}
	
	/*public void WriteFile(String file_path){
		path = file_path;
	}
	public void writeToFile(String textLine ) throws IOException {
		FileWriter write = new FileWriter(path, append_to_file);
		PrintWriter print = new PrintWriter(write);
		
		print.printf( "%s" + "%n" , textLine);

		print.close();
	}
	public static void main(String args) throws IOException {
		String file_name = "/tmp";
		//try {
			//ReadFile file = new ReadFile(file_name);
	//	}
	}*/
	}
