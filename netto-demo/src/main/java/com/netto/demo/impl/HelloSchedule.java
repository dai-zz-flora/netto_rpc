package com.netto.demo.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.netto.schedule.ScheduleParam;
import com.netto.schedule.support.AbstractScheduleTaskProcess;

@Service
public class HelloSchedule extends AbstractScheduleTaskProcess{

    @Override
    protected List selectTasks(ScheduleParam param, Integer curServer) {
        
        return new ArrayList();
    }

    @Override
    protected void executeTasks(ScheduleParam param, List tasks) {
        // TODO Auto-generated method stub
        
    }

}
