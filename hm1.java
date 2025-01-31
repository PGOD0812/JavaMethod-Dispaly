package lib;

public class hm1 {
    public static void main(String[] args) {
        String str = "abcdef";
        System.out.println(reverse(str,0,40 ));
    }
    public static String reverse(String str, int start, int end)
    {
        //校验参数
        //写出正确情况取反即可
        if(!(end < str.length() && str != null && start >= 0 && end > start))
        {
            throw new RuntimeException("参数不正确"); //抛出错误
        }
        char[] chars = str.toCharArray(); //将字符串转为字符数组
        for (int i = start; i < end; i++) {
            char a = chars[i];
            chars[i] = chars[end];
            chars[end--] = a;
        }
        return new String(chars);  //使用chars重新构建一个string
    }
}
