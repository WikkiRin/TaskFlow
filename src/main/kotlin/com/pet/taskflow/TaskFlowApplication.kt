package com.pet.taskflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaskFlowApplication

fun main(args: Array<String>) {
	runApplication<TaskFlowApplication>(*args)
}
