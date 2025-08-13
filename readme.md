# Spring Boot: Security, JWT, Setup

## youtube.com/watch?v=rBNOc4ymd1E
- When we go to localhost:8080, it will first ask to authenticate.
    - For example purposes, on start of this application, we add credentials to H2 database.
- Now we want to authenticate the user and generate jwt such that proceeding endpoint requests can use the jwt.
  This logic is in JwtUtil class `generateToken` method where it will take the username to generate the token.
  Notice that in the `generateToken` method, it calls `createToken` method which uses the jwt library to create token.
    - Recall in the jwt.io site, jwt is broken up in header, payload and secret to sign jwt object which is labelled
      as 'claim' or
      in hashmap
- so basically token will be generated in the form of encrypted string and from that string we can extract our username
  and password.
    - this is where `validateToken` method comes into play where it extracts the username from the token and validate
      that username and verifying if the token is expired
    - the most important here is `generatetoken` and `validatetoken`
- The client will call the WelcomeController's `generateToken` where that endpiont is expecting username and password
  and uses AuthenticationManager
    - note that the AuthenticationManager (which is deprecated) is spring security class which we configured in
      SecurityConfig class. Here is simply a bean with same name.
    - the authenticationManager will authenticate the username and password and on authenticated, generate the jwt.
    - Note that since we enabled spring security, we would have to provide username and password for all endpoints but
      we want only to use jwt so we will have to configure that
        - override the `configure` method to disable authorizing for specific endpoint with `antMatchers` and the rest
          `permitAll`
- So, so far we have configured two endpoints where one requires login and another that does not since that needs to be
  open to take login and generate token
    - that generated token is then used in the endpoint that required authentication in the header.
- note that we have to tell spring boot to look at the header object and extract the jwt which is what JwtFiler class is
  for
    - the jwtfilter is used to authenticate user and validate token.
    - OncePerRequestFilter class makes it so that it will go through here on every endpoint request
    - notice in `doFilterInternal` it has `httpServletRequest.getHeader("Authorization")` which has "Authorization".
      this is the key we set in the header at client side / postman
- note that the jwtfilter needs to be added to security configure file
  - `http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);`
  - we also want stateless: `.sessionCreationPolicy(SessionCreationPolicy.STATELESS);`
- in summary, client calls authenticate endpoint first to generate jwt assuming that the username and password exist in database. all other endpoints we set header (like in postman we add an 'authorization' key value pair in header) and then we can call that endpoint.
  - you can test the stateless authentication (i.e. our input not stored in any cookie or database), by removing the authentication key 
- Files of Focus:
  - SecurityConfig, WelcomeController, AuthRequest, User, JwtFilter, UserRepository, CustomUserDetailsService, JwtUtil, /resources

## youtube.com/watch?v=R76S0tfv36w
- Files of focus:
  - /config, ProductController, Product, UserInfo, UserInfoRepository, ProductService
- Note the changes in Spring Security 3.1
  - in SecurityConfig, there is no more override configure method. Instead, we need a bean of user detail service to define the authentication related stuff, `@Bean public UserDetailsService userDetailsService()`.
    - in here we can define the user details with different types of authorization levels
  - Before version 3.1, the implementation required a configure method whereas in 3.1 we need to replace with SecurityFilterChain.
    - Spring Security Filter Chain class is used and expose as a bean and then configure our authorization stuff, `@Bean public SecurityFilterChain securityFilterChain`.
    - In this example application, ProductController has endpoints.
      - `requestMatchers("/product/welcome", "/product/addNewUser").permitAll()` open for all
      - all the rest of the endpoint, `.requestMatchers("/product/**").authenticated()`, require authentication
- Authorization vs Authentication
  - In the example code, we have `UserDetailsService userDetailsService(PasswordEncoder encoder)` which we manually set two different type of users with different roles. Here we create a user which allows Bob to authenticate. We have roles to authorize or restrict specific endpoint access depending on the type of roles.
    - We can do the authorization customizing with the `@PreAuthorize` annotation in the controller then we need to tell spring security we have set method level authorization with `@EnableMethodSecurity`
- AuthenticationProvider
  - there might be an error if the security config class did not have authentication provider method when you try to log in using an existing user
  - Recall we defined UserDetailService that talks to the db and validate the user, but we need an authenticationProvider to talk to the UserDetailService
    - We need to provide to AuthenticationProvider the user detail service and password encoder. Given that, AuthenticationProvider will talk to UserDetails and generate UserDetails object and set to it. (?)
- In Spring Security 3.0 we need to manually create all the beans (sometimes AuthenticationManager as well) that we set in SecurityConfig class
