package com.mrray.desens.task.entity.dto;

public class AutoDto {

    private static final String[] WEEKS = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

    // 脱敏任务执行周期只有 DAY、WEEK ,MONTH,IN_TIME(指定一个准确时间只执行一次),INTERVAL(间隔执行)
    //@OneOfString(value = {"DAY", "WEEK","MONTH","IN_TIME"}, message = "cycle must be DAY or WEEK or MONTH")
    private String cycle;

    //执行时间 如果cycle是DAY time 是0~23
    // 如果cycle是WEEK time是 1~7
    // 如果cycle是MONTH,范围是1~31
    //@Range(min = 1, max = 7, message = "time must between {min} and {max}")
    private int point;

    private int month;

    private int year;

    //@NotBlank(message = "execTime can't be blank")
    private String execTime;

    private boolean auto;

    private String startTime;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getExecTime() {
        return execTime;
    }

    public void setExecTime(String execTime) {
        this.execTime = execTime;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getCronExp() {

        String[] split = this.execTime.split(":");

        int h = Integer.parseInt(split[0]);
        int m = Integer.parseInt(split[1]);
        int s = Integer.parseInt(split[2]);

        if ("WEEK".equalsIgnoreCase(cycle)) {
            //以周为周期
            return String.format("%s %s %s ? * %s", s, m, h, WEEKS[point - 1]);
        } else if ("DAY".equalsIgnoreCase(cycle)) {
            // 以天为周期
            return String.format("%s %s %s * * ? ", s, m, h);
        } else if ("MONTH".equalsIgnoreCase(cycle)) {
            //以月为周期
            if (point == 0) {//月末的情况
                return String.format("%s %s %s %s * ? ", s, m, h, "L");
            } else {
                return String.format("%s %s %s %s * ? ", s, m, h, point);
            }

        } else if ("IN_TIME".equalsIgnoreCase(cycle)) {
            //指定时间
            return String.format("%s %s %s %s %s ? %s", s, m, h, point, month, year);
        } else {
            return String.valueOf(1000l * ((h * 60 + m) * 60 + s));
        }
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
