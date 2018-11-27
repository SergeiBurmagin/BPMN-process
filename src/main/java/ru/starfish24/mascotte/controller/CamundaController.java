package ru.starfish24.mascotte.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.starfish24.mascotte.service.task.CamundaTaskService;

@RestController
@RequestMapping("starfish24/api")
public class CamundaController {

    @Autowired
    private CamundaTaskService camundaTaskService;

    @RequestMapping(value = "/task-identity", method = RequestMethod.GET)
    public Boolean checkTaskIdentity(@RequestParam("taskId") String taskId, @RequestParam("login") String login) {
        return camundaTaskService.checkTaskIdentity(taskId, login);
    }
}
