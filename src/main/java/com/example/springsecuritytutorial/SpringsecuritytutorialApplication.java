package com.example.springsecuritytutorial;

import com.example.springsecuritytutorial.entity.User;
import com.example.springsecuritytutorial.entity.UserInfo;
import com.example.springsecuritytutorial.repository.UserInfoRepository;
import com.example.springsecuritytutorial.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class SpringsecuritytutorialApplication {

    @Autowired
    private UserRepository repository;

    @PostConstruct
    public void initUsers() {
        List<User> users = Stream.of(
                new User(101, "u1", "e1", "u1"),
                new User(102, "u2", "e2", "u2"),
        new User(103, "u3", "e3", "u3"),
        new User(104, "u4", "e4", "u4")
        ).collect(Collectors.toList());
        repository.saveAll(users);
    }

	public static void main(String[] args) {
		SpringApplication.run(SpringsecuritytutorialApplication.class, args);
	}

}
