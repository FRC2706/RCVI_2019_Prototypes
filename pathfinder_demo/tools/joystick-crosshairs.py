# add compiler stuff here
# https://rosettacode.org/wiki/Joystick_position
# 

import pygame
import sys
import time
import threading
from networktables import NetworkTables
from networktables.util import ntproperty
import logging
import datetime

# print button events to console, True or False
booVerbose = True
# required for nt connection
cond = threading.Condition()
# required for nt connection
notified = [False]

# count the number of frames processed by vision
FrameCounter = 0

# variable to alter pace of path change
intPathPace = 2.0

# choose between moving path and single testing path
booPathSingle = True

# direct from robotpy
def connectionListener(connected, info):
    print(info, '; Connected=%s' % connected)
    with cond:
        notified[0] = True
        cond.notify()

# inspired by from robotpy
class hpMiniPython(object):
    findShipBayLeft = ntproperty('/Vision/findShipBayLeft', False)
    findShipBayCenter = ntproperty('/Vision/findShipBayCenter', False)
    findShipBayRight = ntproperty('/Vision/findShipBayRight', False)
    #angleToTarget = ntproperty('/PathFinder/angleToTarget', 0)
    #distanceToTarget = ntproperty('/PathFinder/distanceToTarget', 0)
    vectorToTarget = ntproperty('/PathFinder/vectorToTarget', [0, 0])
    gameStartSeconds = ntproperty('/Vision/gameStartSeconds', 0)
    secondsSinceStart = ntproperty('/PathFinder/secondsSinceStart', 0)
    FrameCounter = ntproperty('/Vision/FrameCounter', 0)

# setup network tables
logging.basicConfig(level=logging.DEBUG)
#NetworkTables.initialize(server='localhost')
NetworkTables.initialize(server='10.27.6.100')
NetworkTables.addConnectionListener(connectionListener, immediateNotify=True)

# from robotpy
with cond:
    print("Waiting")
    if not notified[0]:
        cond.wait()

# short form for this device for nt
hp = hpMiniPython()

hp.gameStartSeconds = 0.0
hp.secondsSinceStart = 0.0
hp.angleToTarget = 0.0
hp.distanceToTarget = 0.0

# begin pygame stuff / note add hat code later
pygame.init()
 
# Create a clock (for pygame framerating)
clk = pygame.time.Clock()
 
# Grab joystick 0
if pygame.joystick.get_count() == 0:
    raise IOError("No joystick detected")
joy = pygame.joystick.Joystick(0)
joy.init()
 
# Create display
size = width, height = 430, 430
screen = pygame.display.set_mode(size)
pygame.display.set_caption("Joystick Tester - Modified by Merge!!!")
 
# Frame XHair zone
frameRect = pygame.Rect((30, 30), (370, 370))
 
# Generate crosshair 1
crosshair1 = pygame.surface.Surface((10, 10))
crosshair1.fill(pygame.Color("yellow"))
pygame.draw.circle(crosshair1, pygame.Color("blue"), (5,5), 5, 0)
crosshair1.set_colorkey(pygame.Color("yellow"), pygame.RLEACCEL)
crosshair1 = crosshair1.convert()

# Generate crosshair 2
crosshair2 = pygame.surface.Surface((10, 10))
crosshair2.fill(pygame.Color("yellow"))
pygame.draw.circle(crosshair2, pygame.Color("red"), (5,5), 5, 0)
crosshair2.set_colorkey(pygame.Color("yellow"), pygame.RLEACCEL)
crosshair2 = crosshair2.convert()

# Generate button surfaces
writer = pygame.font.Font(pygame.font.get_default_font(), 15)
buttons = {}

# loop through all buttons found
buttonLoopLength = joy.get_numbuttons()
for b in range(buttonLoopLength):
    buttons[b] = [
        writer.render(
            hex(b)[2:].upper(),
            1,
            pygame.Color("yellow"),
            pygame.Color("black")
        ).convert(),
        # Get co-ords: ((width*slot)+offset, offset). Offsets chosen
        #                                             to match frames.
        ((15*b)+60, 370)
    ]

# Generate hat surfaces, broken...
#writer = pygame.font.Font(pygame.font.get_default_font(), 15)
#hats = {}
#for h in range(joy.get_numhats()):
#    hats[h] = [
#        writer.render(
#            hex(h)[2:].upper(),
#            1,
#            pygame.Color("yellow"),
#            pygame.Color("black")
#        ).convert(),
#        ((15*b)+60, 300)
#    ]

# reduce traffic to nt with booleans
booTargetLeft = False # 0
booTargetCenter = False # 3
booTargetRight = False # 2
booSendTarget = False # 7

booTLeftLast = False
booTCenterLast = False
booTRightLast = False

# track the start of the game, push to nt
gameStartTime = datetime.datetime.now()
gameStartSeconds = gameStartTime.hour*3600.0+gameStartTime.minute*60.0+gameStartTime.second+gameStartTime.microsecond/1000000
startSeconds = gameStartSeconds


# main loop
while True:

    # Pump and check the events queue
    pygame.event.pump()

    for events in pygame.event.get():

        #print (events.type) # 10 is button down, 11 is button up, 2 is key down, 3 is key up

        if events.type == pygame.JOYBUTTONUP:
            if events.button is 3:
                booTargetCenter = False
            elif events.button is 0:
                booTargetLeft = False
            elif events.button is 2:
                booTargetRight = False
            elif events.button is 5:
                booSendTarget = False
                hp.secondsSinceStart = 0.0
                hp.angleToTarget = 0.0
                hp.distanceToTarget = 0.0
            elif events.button is 7:
                booSendTarget = False
                hp.secondsSinceStart = 0.0
                hp.angleToTarget = 0.0
                hp.distanceToTarget = 0.0
        elif events.type == pygame.JOYBUTTONDOWN:
            if events.button is 5:
                booSendTarget = True
                booPathSingle = True
                now = datetime.datetime.now()
                startSeconds = now.hour*3600.0+now.minute*60.0+now.second+now.microsecond/1000000
            elif events.button is 7:
                booSendTarget = True
                booPathSingle = False
                now = datetime.datetime.now()
                startSeconds = now.hour*3600.0+now.minute*60.0+now.second+now.microsecond/1000000
 
        elif events.type == pygame.QUIT:
            pygame.quit()
            sys.exit()

    # Black the screen
    screen.fill(pygame.Color("black"))
 
    # Get joystick axes
    w = joy.get_axis(0)
    x = joy.get_axis(1)
    y = joy.get_axis(2)
    z = joy.get_axis(3)
 
    # Blit to the needed coords:
    # w*amplitude+(centre offset (window size/2))-(*hair offset (*h size/2))
    screen.blit(crosshair1, ((w*185)+215-5, (x*185)+215-5))
    screen.blit(crosshair2, ((y*185)+215-5, (z*185)+215-5))
    pygame.draw.rect(screen, pygame.Color("yellow"), frameRect, 1)
 
    # Get and display the joystick buttons
    for b in range(joy.get_numbuttons()):
        indiv = joy.get_button(b)
        if indiv:
            screen.blit(buttons[b][0], buttons[b][1])
            # this maintains the button down logic in network tables
            if b is 3:
                booTargetCenter = True
            elif b is 0:
                booTargetLeft = True
            elif b is 2:
                booTargetRight = True
            elif b is 5:
                booSendTarget = True
                booPathSingle = True
            elif b is 7:
                booSendTarget = True
                booPathSingle = False

    # Write the display
    pygame.display.flip()
    clk.tick(30) # Limit to <=30 FPS

    if booSendTarget:

        # track the time the button is down and display in nt
        now = datetime.datetime.now()
        elapsedSeconds = now.hour*3600.0+now.minute*60.0+now.second+now.microsecond/1000000 - startSeconds
        hp.secondsSinceStart = elapsedSeconds 

        # based on time button down, feed angle and distance to nt
        if booPathSingle:
            #hp.angleToTarget = 27.0
            #hp.distanceToTarget = 6.0
            hp.vectorToTarget = [27.0, 6.0]
        else:
            if intPathPace * 0 <= elapsedSeconds <= intPathPace * 1:
                #hp.angleToTarget = 27.0
                #hp.distanceToTarget = 6.0
                hp.vectorToTarget = [27.0, 6.0]
            elif intPathPace * 1 <= elapsedSeconds <= intPathPace * 2:
                #hp.angleToTarget = 20.0
                #hp.distanceToTarget = 4.5
                hp.vectorToTarget = [20.0, 4.5]
            elif intPathPace * 2 <= elapsedSeconds <= intPathPace * 3:
                #hp.angleToTarget = 14.0
                #hp.distanceToTarget = 3.5
                hp.vectorToTarget = [14.0, 3.5]
            elif intPathPace * 3 <= elapsedSeconds <= intPathPace * 4:
                #hp.angleToTarget = 9.0
                #hp.distanceToTarget = 2.3
                hp.vectorToTarget = [9.0, 2.3]
            elif intPathPace * 4 <= elapsedSeconds <= intPathPace * 5:
                #hp.angleToTarget = 4.0
                #hp.distanceToTarget = 1.2
                hp.vectorToTarget = [4.0, 1.2]
            elif intPathPace * 5 <= elapsedSeconds <= intPathPace * 6:
                #hp.angleToTarget = 1.0
                #hp.distanceToTarget = 1.0
                hp.vectorToTarget = [1.0, 1.0]
    else:
        hp.gameStartSeconds = 0.0
        hp.secondsSinceStart = 0.0
        #hp.angleToTarget = 0.0
        #hp.distanceToTarget = 0.0
        hp.vectorToTarget = [0.0, 0.0]

    # save nt traffic by only sending changes on carbo bay buttons
    if (booTLeftLast == booTargetLeft and booTCenterLast == booTargetCenter and booTRightLast == booTargetRight): 
        pass
    elif booTargetCenter != booTCenterLast and booTargetCenter: # center false to true
        hp.findShipBayCenter = True
    elif booTargetCenter != booTCenterLast and booTargetCenter is False: # center true to false
        hp.findShipBayCenter = False
    elif booTargetCenter is False:
        if booTargetLeft != booTLeftLast and booTargetLeft: # left false to true
            hp.findShipBayLeft = True
        elif booTargetLeft != booTLeftLast and booTargetLeft is False: # left true to false
            hp.findShipBayLeft = False
        elif booTargetRight != booTRightLast and booTargetRight: # left false to true
            hp.findShipBayRight = True
        elif booTargetRight != booTRightLast and booTargetRight is False: # left true to false
            hp.findShipBayRight = False

    # store the current to last, part of the nt traffic saver effort        
    booTLeftLast = booTargetLeft
    booTCenterLast = booTargetCenter
    booTRightLast = booTargetRight
    booSTargetLast = booSendTarget

    # send the number of frames processed by vision to the nt
    FrameCounter = FrameCounter + 1
    hp.FrameCounter = FrameCounter

    # determine game elapsed time and put to nt
    now = datetime.datetime.now()
    elapsedSeconds = now.hour*3600.0+now.minute*60.0+now.second+now.microsecond/1000000
    hp.gameStartSeconds = elapsedSeconds - gameStartSeconds
    
pygame.quit()

