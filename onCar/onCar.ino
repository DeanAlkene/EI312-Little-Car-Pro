#define LEFT_AHEAD 9
#define LEFT_BACK 10
#define RIGHT_AHEAD 12
#define RIGHT_BACK 13
int RUN = 300;
int TURN = 200;
void goAhead();
void goLeftAhead();
void goRightAhead();
void turnLeft();
void turnRight();
void park(); 
void goBack(); 
void goLeftBack();
void goRightBack();

void setup()
{
    Serial.begin(9600);
    pinMode(LEFT_AHEAD,OUTPUT);
    pinMode(LEFT_BACK, OUTPUT);
    pinMode(RIGHT_AHEAD, OUTPUT);
    pinMode(RIGHT_BACK, OUTPUT);
    digitalWrite(LEFT_AHEAD, LOW);
    digitalWrite(LEFT_BACK, LOW);
    digitalWrite(RIGHT_AHEAD, LOW);
    digitalWrite(RIGHT_BACK, LOW);
    Serial.println("CONNECTED!");
}
char operation;
void loop()
{
    if (Serial.available() > 0)
    {
        operation = Serial.read();
        switch(operation)
        {
          case 'W':
            Serial.println("GO STRAIGHT");
            goAhead();
            break;
          case 'A':
            Serial.println("TURN LEFT");
            turnLeft();
            break;
          case 'D':
            Serial.println("TURN RIGHT");
            turnRight();
            break;
          case 'X':
            Serial.println("GO BACK");
            goBack();
            break;
          case 'S':
            Serial.println("STOP");
            park();
            break;
          case 'Q':
            Serial.println("GO LEFT AHEAD");
            goLeftAhead();
            break;
          case 'E':
            Serial.println("GO RIGHT AHEAD");
            goRightAhead();
            break;
          case 'Z':
            Serial.println("GO LEFT BACK");
            goLeftBack();
            break;
          case 'C':
            Serial.println("GO RIGHT BACK");
            goRightBack();
            break;
        }
    }
}

void goAhead()
{
    digitalWrite(RIGHT_BACK,LOW);
    digitalWrite(LEFT_BACK,LOW);
    analogWrite(RIGHT_AHEAD,RUN);
    analogWrite(LEFT_AHEAD,RUN);
}
void goLeftAhead()
{
    digitalWrite(RIGHT_BACK,LOW);
    digitalWrite(LEFT_BACK,LOW);
    analogWrite(RIGHT_AHEAD,RUN);
    digitalWrite(LEFT_AHEAD,LOW);
}
void goRightAhead()
{
    digitalWrite(RIGHT_BACK,LOW);
    digitalWrite(LEFT_BACK,LOW);
    digitalWrite(RIGHT_AHEAD,LOW);
    analogWrite(LEFT_AHEAD,RUN);
}
void turnLeft()
{
    digitalWrite(RIGHT_BACK,LOW);
    digitalWrite(LEFT_AHEAD,LOW);
    analogWrite(RIGHT_AHEAD,RUN);
    analogWrite(LEFT_BACK,RUN);
}
void turnRight()
{
    digitalWrite(LEFT_BACK,LOW);
    digitalWrite(RIGHT_AHEAD,LOW);
    analogWrite(LEFT_AHEAD,RUN);
    analogWrite(RIGHT_BACK,RUN);
}
void park()
{
    digitalWrite(LEFT_BACK, LOW);
    digitalWrite(RIGHT_BACK, LOW);
    digitalWrite(LEFT_AHEAD, LOW);
    digitalWrite(RIGHT_AHEAD, LOW);
}
void goBack()
{
    digitalWrite(RIGHT_AHEAD,LOW);
    digitalWrite(LEFT_AHEAD,LOW);
    analogWrite(RIGHT_BACK,RUN);
    analogWrite(LEFT_BACK,RUN);
}
void goLeftBack()
{
    digitalWrite(RIGHT_AHEAD,LOW);
    digitalWrite(LEFT_AHEAD,LOW);
    analogWrite(RIGHT_BACK,RUN);
    digitalWrite(LEFT_BACK,LOW);
}
void goRightBack()
{
    digitalWrite(RIGHT_AHEAD,LOW);
    digitalWrite(LEFT_AHEAD,LOW);
    digitalWrite(RIGHT_BACK,LOW);
    analogWrite(LEFT_BACK,RUN);
}