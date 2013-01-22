package jp.angeworm.ensyuD.compiler;

import java.util.Random;

public class LabelGenerater {
	private Random rnd = null; 
	
	public LabelGenerater() {
		rnd = new Random();
	}

	public LabelGenerater(long seed) {
		rnd = new Random(seed);
	}
	
	public String makeLabel() {
		return makeLabel("");
	}
	
	public String makeLabel(String prefix) {
		StringBuilder sb = new StringBuilder(prefix);
		String seeds = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		
		for(int i = sb.length(); i < 8; i++) {
			sb.append(seeds.charAt(rnd.nextInt() % seeds.length()));
		}
		return sb.toString();
	}
}
