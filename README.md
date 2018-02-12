# acapitaladay
A simple web application that helps you learn the capitals of the countries of the world.
Shows a new country every day and allows you to guess the capital.

Built entirely in Scala using the [Play Framework](https://www.playframework.com/), [Scala.JS](https://www.scala-js.org/) with the FRP framework [Binding.scala](https://github.com/ThoughtWorksInc/Binding.scala).

## Running
To start with, you will need SBT installed. Go [here](https://www.scala-sbt.org/download.html) and follow the instructions for your platform.

Once SBT is installed, you can run the application in dev mode (automatically rebuilds Scala and Scala.JS code on page load):

    sbt run

Then go to http://localhost:9000/
