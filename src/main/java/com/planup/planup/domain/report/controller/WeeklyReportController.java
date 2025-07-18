package com.planup.planup.domain.report.controller;

import com.planup.planup.domain.report.service.WeeklyReportService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;


}
