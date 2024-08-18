package com.study.seckill.common.util;

public class IpUtil {
    /**
     * 将字符串形式IP地址127.0.0.1转换10234564321
     *
     * @param strIP
     * @return
     */
    public static long ip2Long(String strIP) {
        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = strIP.indexOf(".");
        int position2 = strIP.indexOf(".", position1 + 1);
        int position3 = strIP.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIP.substring(0, position1));
        ip[1] = Long.parseLong(strIP.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIP.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIP.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }
    /**
     * 将字符串形式IP地址转换long类型
     *
     * @param ip
     * @return
     */
    public static long getIp2long(String ip) {
        ip = ip.trim();
        String[] ips = ip.split("\\.");
        long ip1 = Integer.parseInt(ips[0]);
        long ip2 = Integer.parseInt(ips[1]);
        long ip3 = Integer.parseInt(ips[2]);
        long ip4 = Integer.parseInt(ips[3]);
        long ip2long = 1L * ip1 * 256 * 256 * 256 + ip2 * 256 * 256 + ip3 * 256 + ip4;
        return ip2long;
    }
    /**
     * 判断一个ip地址是否在某个ip段范围内
     *
     * @param ip
     * @param startIP
     * @param endIP
     * @return
     */
    public static boolean ipExistsInRange(String ip, String startIP, String endIP) {
        return (getIp2long(startIP) <= getIp2long(ip)) && (getIp2long(ip) <= getIp2long(endIP));
    }
    /**
     * test：给定的ip地址是否在某个ip段范围内
     *
     * @param args
     */
    public static void main(String[] args) {
        //10.10.10.116 是否属于固定格式的IP段10.10.1.00-10.10.255.255
        String ip = "10.10.10.116";
        String startIP = "10.10.1.00";
        String endIP = "10.10.255.255";
        boolean exists = ipExistsInRange(ip, startIP, endIP);
        System.out.println(ip2Long(ip));
        System.out.println(getIp2long(ip));
        System.out.println(exists);
    }
}
