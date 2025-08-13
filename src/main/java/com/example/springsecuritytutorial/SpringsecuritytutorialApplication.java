package com.example.springsecuritytutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringsecuritytutorialApplication {

//    @Autowired
//    private ProductService productService;
//
//    @Autowired
//    private UserInfoRepository repository;

//    @PostConstruct
//    public void initUsers() {
//        List<UserInfo> users = Stream.of(
//                new UserInfo("u1", "e1", "u1", "ADMIN"),
//                new UserInfo("u2", "e2", "u2", "USER"),
//                new UserInfo("u3", "e3", "u3", "USER"),
//                new UserInfo("u4", "e4", "u4", "USER")
//        ).toList();
//        for (UserInfo userInfo : users) {
//            productService.addUser((userInfo));
//        }
//    }

    public static void main(String[] args) {
        SpringApplication.run(SpringsecuritytutorialApplication.class, args);
    }

}
