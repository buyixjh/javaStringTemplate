package com.wurenzhi;

/**
 * 测试类
 */
@StrTemplate
public class Person {
	
	// String aaa = "1";
	// String ccc = aaa + " to ${aaa}";//无效 因为加载顺序不一致
	
	public void localMethod() {
		// String bbb = "666";
		// String aaa = "${bbb} to aaa";
		// new class2().mytest222(String.valueOf("${bbb} to param"), 1, null);
		// new class2().mytest222("${bbb} to param", 1, null);
		// String s = new class2().mytest222("${bbb} to param", 1, null);
		// String s2 = new class2().mytest222(String.valueOf(String.valueOf("${bbb} to param")), 1, null);
		
		// String aaa2 = "${bbb} 2 is not eq 888${bbb}";
		// String s2323 = new class2().mytest222("${bbb} to param", 1, null);
		// System.out.println("${bbb} is not eq 777");
		// if ("${bbb}".equals("2")) System.out.println("${bbb} if meth");
		// while ("${bbb}".equals("2")) System.out.println("${bbb} if while");
		// for (int i = Integer.valueOf("${bbb}"); i < Integer.valueOf("${bbb}"); i += Integer.valueOf("${bbb}")) {
		// 	System.out.println("${bbb} if for");
		// }
		// new Person().mytest333("${bbb} to new Person test333");
		//
		// switch ("${bbb}") {
		// 	case "1":
		// 		System.out.println("${bbb}");
		// 		break;
		// 	case "2":
		// 		System.out.println("${bbb}");
		// 		break;
		// 	default:
		// 		System.out.println("${bbb}");
		// 		break;
		// }
		// do {
		// 	System.out.println("${bbb}");
		// } while ("${bbb}".equals("2"));
		//
		// new Thread(() -> {
		// 	System.out.println("${bbb}");
		// });
		// final Thread thread = new Thread(new Runnable() {
		// 	@Override
		// 	public void run() {
		// 		System.out.println("${bbb}Runnable");
		// 	}
		// });
		// new Thread(new Runnable() {
		// 	@Override
		// 	public void run() {
		// 		System.out.println("${bbb}Runnable");
		// 	}
		// });
		//
		// try (final FileInputStream stream = new FileInputStream("${bbb}99")) {
		// 	int i = 1 / 0;
		// 	System.out.println("${bbb} 1");
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// 	System.out.println("${bbb} 2");
		// } finally {
		// 	System.out.println("${bbb} 3");
		// }
		//
		// synchronized (this.toString() + "${bbb} sync") {
		// 	System.out.println("${bbb} 1");
		// }
		
		// return "return_${bbb}"; // 未找到该id的案例
	}
	
	// class class2 {
	//
	// 	String bbbc = "1";
	//
	// 	public String mytest222(String param, Integer integer, Person person) {
	// 		// String bbbc = "1";
	// 		return bbbc;
	// 		// return bbb + "${bbbc}_mytest222_return";
	// 	}
	//
	// 	public void aaa() {
	// 		// String bbbc = "1";
	// 		throw new RuntimeException("${bbbc}yiChang");
	// 	}
	//
	// }
	
	// public String mytest333(String param) {
	// 	return param;
	// }
	//
	// public Person() {
	//
	// 	assert "${bbb}" == "" : "${bbb}_assert";
	// }
	
}