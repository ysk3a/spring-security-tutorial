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
- The client will call the WelcomeController's `generateToken` where that endpoint is expecting username and password
  and uses AuthenticationManager
    - note that the AuthenticationManager (which is deprecated) is spring security class which we configured in
      SecurityConfig class. Here is simply a bean with same name.
    - the authenticationManager will authenticate the username and password and on authenticated, generate the jwt.
    - Note that since we enabled spring security, we would have to provide username and password for all endpoints, but
      we want only to use jwt so we will have to configure that
        - override the `configure` method to disable authorizing for specific endpoint with `antMatchers` and the rest
          `permitAll`
- So, so far we have configured two endpoints where one requires login and another that does not since that needs to be
  open to take login and generate token
    - that generated token is then used in the endpoint that required authentication in the header.
- note that we have to tell spring boot to look at the header object and extract the jwt which is what JwtFiler class is
  for
    - the jwtFilter is used to authenticate user and validate token.
    - OncePerRequestFilter class makes it so that it will go through here on every endpoint request
    - notice in `doFilterInternal` it has `httpServletRequest.getHeader("Authorization")` which has "Authorization".
      this is the key we set in the header at client side / postman
- note that the jwtFilter needs to be added to security configure file
  - `http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);`
  - we also want stateless: `.sessionCreationPolicy(SessionCreationPolicy.STATELESS);`
- in summary, client calls authenticate endpoint first to generate jwt assuming that the username and password exist in database. all other endpoints we set header (like in postman we add an 'authorization' key value pair in header) and then we can call that endpoint.
  - you can test the stateless authentication (i.e. our input not stored in any cookie or database), by removing the authentication key 
- Files of Focus:
  - SecurityConfig, WelcomeController, AuthRequest, User, JwtFilter, UserRepository, CustomUserDetailsService, JwtUtil, /resources

## youtube.com/watch?v=R76S0tfv36w
- Files of focus:
  - /config, ProductController, Product, UserInfo, UserInfoRepository, ProductService
- Note the changes in Spring Boot 3.1
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
- In Spring Boot 3.0 we need to manually create all the beans (sometimes AuthenticationManager as well) that we set in SecurityConfig class

## youtube.com/watch?v=NcLtLZqGu2M
- Before continuing with the video, copied the git repo into here and removed other files. Setting up MySQL for spring boot application to run by starting the mysql server (in Windows10 in services window)
  - open mysql workbench or whatever dbm application to see changes.
- Some collections of notes from online:
  - You don't want to use @Data as this generates equals, hashCode and toString methods, which in the case of jpa entities should be hand generated.
    - stackoverflow.com/questions/34241718/lombok-builder-and-jpa-default-constructor

### running it
- Anyway, to run this example before the jwt addition 4ec3a2e62277ef5d2d318bae92ec2db2da73f16a:
  - we need to have mysql with the database 'jwttutorial' ready, and we have mysql setup with mysql's username and password to root and admin for demo purposes
    - normally you would put username and password as env variables to not expose these secret values
  - I could not figure out how to add dummy values on spring boot startup so the pre-setup would be to use some rest client to add users.
    - I used Bruno/postman and created a new http request with POST url of (`http://`)(`localhost:8080/products/new`) with body containing json attributes of `UserInfo` without id.
    - Now you can access certain endpoints using the credentials depending on the authorization (roles) levels.
- Not to be confused, we would normally have a UserInfoController for addition of new user instead of having that endpoint in Products. This is just following the videos.

### with JWT
- So from this point we want to use jwt so that we don't need to sign in for every endpoint access.
  - `authenticateAndGetToken` endpoint will get credential from request body to generate jwt
  - So client will call `authenticateAndGetToken` which calls JwtService's `createToken` which creates the decoded header and payload using the JWT signature verification or some secret (in this case DUMMY_SECRET)
    - but where does one use `encryptWith` method from the JwtBuilder?
  - We also need to allow `/products/authenticate` endpoint to also be exposed so that anyone can enter their credentials
    - Then in rest client we can call that endpoint (`http://`)(`localhost:8080/products/authenticate`) with json body matching AuthRequest object
    - but if you enter wrong credential we should not get useful token so we need add restriction despite making it exposed by first authenticating the credential 
      - `Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));`
      - we should only return token on successful authentication i.e. the credential exists in the db.
  - note that in spring boot 3, we need to explicitly define bean of authenticationManager in our SecurityConfig. (why?)
- Now to use the jwt when calling the endpoint we need to add that jwt that was generated from /products/authenticate into the other endpoints which the auth set
  - in Bruno or in Postman, in the Auth or Authorization tab, set type as Bearer Token and enter the jwt token there.
    - or set `{{token}}` as token variable under Auth tab and in the authenticate endpoint use script to set env/collection/global variable
  - But you might get an error without telling spring authentication manager on what is the token we gave it, that is we need to tell spring boot to verify that the token matches and is of valid user
    - this is where JwtAuthFilter is used.
      - `doFilterInternal`
        - we want to check `request.getHeader("Authorization")` where the "Authorization" is in the bruno/postman's Auth tab
        - Since it is of Bearer type the token spring boot in `doFilterInternal` will receive as `bearer {whatever_tokne}` which is why we need to extract the token and from the token extra whatever data in the jwt.
        - e.g. subject or username we can extract from `extractClaim`.
          - then by taking the extracted username, we can go through the UserDetailService to load the User detail object and valid that object which we can pass to the jwtService.validateToken which checks the token and UserDetail username matches
          - but is this normally sufficient for validation?
      - `SecurityContextHolder.getContext().getAuthentication()` 
          - see spring security internal flow video!
  - `.authenticationProvider(authenticationProvider()).addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)` is required as we want to tell spring boot to use authFilter first before spring security's UsernamePasswordAuthenticationFilter 
  - session is set to be stateless in SecurityConfig since we don't want to keep anything in cookies or anything
  - question: not sure about the order of all the dot notation methods in `securityFilterChain`?
- Client -> authenticate endpoint -> generate Token -> return to client token -> use token when sending other request -> spring boot verify and validate token


## youtube.com/watch?v=Wp4h_wYXqmU
- So far we have spring boot application that can authenticate and authorize restricted endpoints. however, for all authenticated request, we need to input username and password.
  - instead we use jwt and pass that token to the backend which is then verified before responding back.
  - but we have not implemented refresh token because of a certain amount of the time passes beyond the expiry date we set for the jwt, client needs to send another authentication request when the token expires.
    - This is where RefreshTokenService and `authenticateAndGetToken` and `refreshToken` methods are the key changes.
    - Flow: User will log in via the /login endpoint and that will return a res.body.accessToken and res.body.token json object where the accessToken is jwt and token is the refreshToken we need to refresh on expiry of accessToken.
    - Note, you might get exception when trying to log in again when jwt is not expired (e.g. Duplicate entry '3' for key refreshtoken)
  - Note in the rest client like bruno, the test script is updated to take account the response object being JwtResponse.
- For the rest client side in bruno see the private repo `github.com/ysk3a/spring-security-tutorial-api-collections/`
  - note the `scripts`/`test` contents as they automatically set the token/accessToken on authentication/refreshing
  - any changes in the repo (or if locally in the ysk3a /restclient in /springboot folder) need to do any git related stuff for changes
  - youtube.com/watch?v=vbykqtXW60M