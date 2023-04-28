# Team CHAAMY Final Project

This is the codebase for the final project in  Software Testing & Debugging, Spring 2023 course at Johns Hopkins University.

The purpose of this project is to test an open-source application systematically and thoroughly.

The subject software application we chosen is [bralax/gradescope_autograder](https://github.com/bralax/gradescope_autograder) in EN.500.112 Gateway Computing: JAVA at Johns Hopkins University to grade student project submissions.


## Authors

- [@anaemeribe](https://www.github.com/anaemeribe)
- [@fuaad001](https://www.github.com/fuaad001)
- [@ayousuf23](https://www.github.com/ayousuf23)


## Running Tests

To run tests, run the following command. This will also generate jacoco reports as a viewable HTML file in `./build/jacocoHTML/index.html`.

```bash
  ./gradlew test
```

To generate PIT test results for mutation testing, run the following command. This will also generate pit reports as a viewable HTML file found in `./build/reports/pitest/index.html`.

```bash
  ./gradlew pitest
```
## Documentation

[Project Proposal](https://docs.google.com/document/d/1y7Nj63hA9nX0882_4wU4J_yjfuRCz17b8tP-atdxUJc/edit?usp=sharing)

[Project Presentation](https://docs.google.com/presentation/d/1qnXa3ZHfIQD5vpV0Pg7dEClKrKjrCUkXlw29gfW4nVs/edit?usp=sharing)