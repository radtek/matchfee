package org.wxjs.matchfee.modules.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestObj {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Human human = new Human();
		
		human.setAge("11");
		human.setName("李四");
		human.setSex("男");
		
		Field[] fields = human.getClass().getDeclaredFields();
		
		for(Field f : fields){
			System.out.println(f.getName());
			String name = f.getName();
			name = name.substring(0,1).toUpperCase()+name.substring(1);
			try {
				Method mGet = human.getClass().getMethod("get"+name);
				String value = (String) mGet.invoke(human);
				
				Method mSet = human.getClass().getMethod("set"+name);
				mSet.invoke(human, new Object[] {new String("xxx")});
				
				System.out.println("value:"+value);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(Field f : fields){
			System.out.println(f.getName());
			String name = f.getName();
			name = name.substring(0,1).toUpperCase()+name.substring(1);
			try {
				Method mGet = human.getClass().getMethod("get"+name);
				String value = (String) mGet.invoke(human);
				
				System.out.println("value:"+value);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}


class Human{
	private String name;
	private String age;
	private String sex;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
}