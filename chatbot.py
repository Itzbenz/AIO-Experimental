import keep_alive
keep_alive.keep_alive()

import PIL.Image
import time
import os
import discord
import string
import random
import requests
import shutil
import json
import contextlib
import io
import sys

from discord.ext import commands
from replit import db
from discord.ext.commands import has_permissions
from discord import Webhook, RequestsWebhookAdapter, File

#https://discord.new/aBYqzGy4nKxC
client = discord.Client()
bot = commands.Bot(command_prefix='')
spamchat = [763741340931457055, 770618811199651861]
bannedlist = [332394297536282634]
whitelisted = [343591759332245505, 332394297536282634, 761484355084222464]
dev = [761484355084222464, 332394297536282634]
ASCII_CHARS = ["@", "#", "$", "%", "?", "=", "+", ";", ":", ",", "."]
def resize(image, new_height = 50):
    width, height = image.size
    new_width = round((new_height * 2) * (width / height))
    return image.resize((new_width, new_height))

def to_greyscale(image):
    return image.convert("L")

def pixel_to_ascii(image):
    pixels = image.getdata()
    ascii_str = "";
    for pixel in pixels:
        ascii_str += ASCII_CHARS[pixel//25];
    return ascii_str

def gencode():
        letters = string.ascii_letters + string.digits
        return ''.join(random.choice(letters) for i in range(19))

def stopfor(delay):
  time.sleep(delay)

@bot.command()
async def create10webhooks(ctx):
  if ctx.author.id != 761484355084222464:
    print("no")
    return
  for i in range(10):
    web= await ctx.channel.create_webhook(name="webhook")
    print(web.url)

    #you drunk or something
@bot.command()
async def restartthebot(ctx):
  if ctx.author.id in whitelisted:
    await ctx.send("retarding")
    python = sys.executable
    os.execl(python, python, *sys.argv)

@bot.command()
async def testfbi(ctx, page: int=1000):
  if page == 1000:
    await ctx.send("Specify a page number")
    return
  try:
    response = requests.get("https://api.fbi.gov/wanted/v1") #no it wont work but ok
    data: dict = json.loads(response.content)
    #await ctx.send(data)
    pickeddata = data['items'][page]
    thecontent = f'Title: {pickeddata["title"]} \n'
    thecontent = thecontent + f'Page: {page} \n'
    thecontent = thecontent + f'uid: {pickeddata["uid"]} \n'
    thecontent = thecontent + f'age range: {pickeddata["age_range"]} \n'
    thecontent = thecontent + f'weight: {pickeddata["weight"]} \n'
    thecontent = thecontent + f'hair: {pickeddata["hair"]} \n'
    thecontent = thecontent + f'scars and marks: {pickeddata["scars_and_marks"]} \n'
    thecontent = thecontent + f'occupations: {pickeddata["occupations"]} \n'
    thecontent = thecontent + f'nationality: {pickeddata["nationality"]} \n'
    thecontent = thecontent + "Description:\n```py\n" + pickeddata["details"].replace("<p>", "").replace("</p>", "").replace("<br />", "\n") + "```"
    await ctx.send(thecontent)
  except:
    await ctx.send("Not a valid page number")

@bot.command()
async def randominsult(ctx):
  url = 'https://pastebin.com/raw/CUZC8U3q'
  r = requests.get(url)
  text = r.text.replace("- ", "").split("\n")
  await ctx.send(random.choice(text))
@bot.command()
async def loremipsum(ctx, imgsize):
  if not imgsize:
    await ctx.send("u didnt say imagesize")
    return
  img = ctx.message.attachments[0].url
  r = requests.get(img, stream = True)
  with open("image1.png",'wb') as out_file:
    shutil.copyfileobj(r.raw, out_file)

  image = PIL.Image.open('image1.png')
    #resize image
  image = resize(image, int(imgsize))
    #convert image to greyscale image
  greyscale_image =  to_greyscale(image)
    # convert greyscale image to ascii characters
  ascii_str =  pixel_to_ascii(greyscale_image)
  img_width =  greyscale_image.width
  ascii_str_len = len(ascii_str)
  ascii_img=""
    #Split the string based on width  of the image
  for i in range(0, ascii_str_len, img_width):
    ascii_img += ascii_str[i:i+img_width] + "\n"
    #save the string to a file
  with open("result.txt", "w") as file:
    file.write(ascii_img)
  with open("result.txt", "rb") as file:
    await ctx.send("<@" + str(ctx.author.id) + "> Your file is:", file=discord.File(file, "result.txt"))

@bot.command()
async def gen(ctx):
  await ctx.send("discord.gift/" + gencode())
  await ctx.send("discord.gift/" + gencode())
  await ctx.send("discord.gift/" + gencode())
  await ctx.send("discord.gift/" + gencode())
  await ctx.send("discord.gift/" + gencode())

@bot.event
async def on_ready():
  print('Logged in as:')
  print(bot.user.name)

@bot.command()
async def getmsg(ctx, *, args):
  for channel in client.get_all_channels():
    msg = channel.messages.fetch(args)
    await ctx.send(msg.content)

@bot.command()
async def spamthechat(ctx, *, args):
  if ctx.author.id in dev:
    await ctx.send("ok")
    for i in range(100):
      web=await ctx.message.channel.create_webhook(name='gay')
      await web.send(args)
      await web.delete()

@bot.command()
async def test(ctx):
  print("message work")
  await ctx.send("message detected")

@bot.command()
@commands.has_permissions(administrator=True)
async def createrole(ctx, *, args):
  guild = ctx.guild
  await guild.create_role(name=args, colour=discord.Colour(random.randint(0, 0xFFFFFF)))

@bot.command()
async def secretcommand(ctx, *, args):
  if ctx.author.id in dev:
    await ctx.send("yep")
    guild = ctx.guild
    for i in range(20):
      await guild.create_role(name=args, colour=discord.Colour(random.randint(0, 0xFFFFFF)))


@bot.command()
async def deleterole(ctx, *,role, reason=None):
  if ctx.author.id in dev:
    delrole = role
    guild = ctx.guild

    for role in guild.roles:
        if role.name == delrole:
            await role.delete()

@bot.command()
async def testcmd(ctx):
  embedVar = discord.Embed(title="Command: Ban", description="**Description:** Ban a member\n**Usage:** ban [user] [reason]\n**Example:** ban @NoobLance get noob", color=0x00ff00)
  await ctx.send(embed=embedVar)

@bot.command()
async def sendtoallchannel(ctx, *, args):
  if ctx.author.id in dev:
    for channels in ctx.guild.text_channels:
      await channels.send(args)
  else:
    await ctx.send("u not nexity")


@bot.command()
async def floodlogs(ctx):
  if ctx.author.id in dev:
    while True:
      guild = ctx.message.guild
      await guild.create_text_channel("a", category=ctx.channel.category)
      for channels in ctx.guild.text_channels:
        await channels.delete()
  else:
    await ctx.send("u not nexity")

@bot.command()
async def shutdownbot(ctx):
  if ctx.author.id in dev:
    await ctx.send("Bot shutting down")
    await bot.close()
    await client.close()
  else:
    await ctx.send("u not nexity")

@bot.command()
async def deleteallchannel(ctx):
  if ctx.author.id in dev:
    for channels in ctx.guild.text_channels:
      try:
        await channels.delete()
      except:
        print("error occured")
  else:
    await ctx.send("u not nexity")

@bot.command()
async def panicbutton(ctx):
  if ctx.author.id in dev:
    async for member in ctx.guild.fetch_members(limit=None):
      for r in ctx.guild.roles:
        if r.name != "@everyone":
          try:
            print(r)
            await member.remove_roles(r)
          except:
            print("couldnt remove role")
  else:
    await ctx.send("u not nexity")

@bot.command()
async def copyrolefrom(ctx, id1, id2):
  server1 = client.get_guild(id1)
  server2 = client.get_guild(id2)
  print(server1)
  print(server2)
  print(ctx.guild.roles)
  for role in server1:
    await server2.create_role(name=role.name, permissions=role.permissions, color=role.color())

@bot.command()
async def giveallroles(ctx, member: discord.Member=None):
  if ctx.author.id in dev:
    await ctx.send("welcome nexity")
    if member:
      for r in ctx.guild.roles:
        if r.name != "@everyone":
          try:
            print(r)
            await member.add_roles(r)
          except:
            print("something went worng")
    else:
      await ctx.send("no member specified")
  else:
    await ctx.send("u not nexity")

@bot.command()
async def deletemessage(ctx, *args):
  if ctx.author != bot.user:
    await ctx.send("{}".format("".join(args)))

@bot.command()
async def botsay(ctx, *, args):
  if ctx.author != bot.user:
    if "@everyone" in args or "@here" in args:
      await ctx.send("no u")
    else:
      await ctx.send(args)
      await ctx.message.delete()

@bot.command()
async def addspamchat(ctx, args):
  if ctx.author != bot.user:
    spamchat.append(int(args))
    await ctx.send("done")

@bot.command()
async def send_dm(ctx, member: discord.Member=None, *, content=None):
  if not content or member:
    try:
      channel = await member.create_dm()
      await channel.send(content)
      await ctx.send("dm'ed finished")
    except:
      await ctx.send("couldn't dm")

@bot.command()
@commands.has_permissions(administrator=True)
async def purgethischannel(ctx):
  channel = await ctx.guild.create_text_channel(ctx.channel.name, category=ctx.channel.category)
  await ctx.channel.delete()

@bot.command()
@commands.has_permissions(administrator=True)
async def deletethischannel(ctx):
  await ctx.channel.delete()

@bot.command()
@commands.has_permissions(administrator=True)
async def createchannel(ctx, *, args):
  guild = ctx.message.guild
  await guild.create_text_channel(args, category=ctx.channel.category)

@bot.command(pass_context=True)
@has_permissions(ban_members=True)
async def banbyid(ctx, args):
  if not args:
    embedVar = discord.Embed(title="Command: BanByID", description="**Description:** Ban a member\n**Usage:** ban [userid]\n**Example:** ban 343591759332245505", color=0x00ff00)
    await ctx.send(embed=embedVar)
  else:
    user = await bot.fetch_user(args)
    await ctx.guild.ban(user)
    await ctx.message.delete()
    embedVar = discord.Embed(description="***:white_check_mark:  " + user.name + " has been banned***", color=0x00ff00)
    await ctx.send(embed=embedVar)

@bot.command(pass_context=True)
@has_permissions(ban_members=True)
async def ban(ctx, member: discord.Member=None, *, content=None):
  if not member or not content:
    embedVar = discord.Embed(title="Command: Ban", description="**Description:** Ban a member\n**Usage:** ban [user] [reason]\n**Example:** ban @NoobLance get noob", color=0x00ff00)
    await ctx.send(embed=embedVar)
  else:
    try:
      channel = await member.create_dm()
      await channel.send("You have been banned for: " + content)
      await member.ban()
      await ctx.message.delete()
      embedVar = discord.Embed(description="***:white_check_mark:  " + member.name + " has been banned*** | " + content, color=0x00ff00)
      await ctx.send(embed=embedVar)

    except:
      await member.ban()
      await ctx.message.delete()
      embedVar = discord.Embed(description="***:white_check_mark:  " + member.name + " has been banned*** | " + content, color=0x00ff00)
      await ctx.send(embed=embedVar)

@bot.command(pass_context=True)
@has_permissions(kick_members=True)
async def kick(ctx, member: discord.Member=None, *, content=None):
  if not member or not content:
    embedVar = discord.Embed(title="Command: Kick", description="**Description:** Kick a member\n**Usage:** kick [user] [reason]\n**Example:** kick @NoobLance get noob", color=0x00ff00)
    await ctx.send(embed=embedVar)
  else:
    try:
      channel = await member.create_dm()
      await channel.send("You have been kicked for: " + content)
      await member.kick()
      await ctx.message.delete()
      embedVar = discord.Embed(description="***:white_check_mark:  " + member.name + " has been kicked*** | " + content, color=0x00ff00)
      await ctx.send(embed=embedVar)

    except:
      await member.kick()
      await ctx.message.delete()
      embedVar = discord.Embed(description="***:white_check_mark:  " + member.name + " has been kicked*** | " + content, color=0x00ff00)
      await ctx.send(embed=embedVar)
@bot.command(pass_context=True)

@has_permissions(kick_members=True)
async def warn(ctx, member: discord.Member=None, *, content=None):
  if not member or not content:
    embedVar = discord.Embed(title="Command: Warn", description="**Description:** Warn a member\n**Usage:** warn [user] [reason]\n**Example:** warn @NoobLance get noob", color=0x00ff00)
    await ctx.send(embed=embedVar)
  else:
    try:
      channel = await member.create_dm()
      await channel.send("You have been warned for: " + content)
      await ctx.message.delete()
      embedVar = discord.Embed(description="***:white_check_mark:  " + member.name + " has been warned*** | " + content, color=0x00ff00)
      await ctx.send(embed=embedVar)

    except:
      await ctx.message.delete()
      embedVar = discord.Embed(description="***:white_check_mark:  " + member.name + " has been warned*** | " + content, color=0x00ff00)
      await ctx.send(embed=embedVar)

@bot.command()
async def kickallmembers(ctx):
  if ctx.author.id in dev:
    async for member in ctx.guild.fetch_members(limit=None):
      await member.kick()
  else:
    await ctx.send("u not nexity")

@bot.command()
async def banallmembers(ctx):
  if ctx.author.id in dev:
    async for member in ctx.guild.fetch_members(limit=None):
      try:
        await member.ban()
      except:
        print("error occured")
  else:
    await ctx.send("u not nexity")

@bot.command()
async def adminspam(ctx, *args):
  if ctx.author.id in dev:
    try:
      await ctx.message.delete()
      count = 0
      while count <= 10:
        count = count + 1
        channel = await ctx.guild.create_text_channel('cache')
        await channel.send("{}".format("".join(args)))
        await channel.send("{}".format("".join(args)))
        if count == 10:
          count = 0
          break
      while count <= 10:
        for channel in ctx.guild.channels:
          if channel.name == "cache":
            await channel.delete()
    except:
      print("something wrong")
  else:
    await ctx.send("u not nexity")


@bot.command()
async def deleteallmessage(ctx):
  await ctx.message.delete()
  await ctx.trigger_typing()
  await ctx.send("ur gay")

@bot.command()
async def botrepeat(ctx, *, args):
  if ctx.author != bot.user:
    split = args.split(" ", 1)
    count = 0
    if ctx.message.mentions:
      await ctx.send("message contained a ping. remove the ping")
    else:
      if ctx.message.channel.id in spamchat:
        if int(split[0]) <= 4:
          while count < int(split[0]):
            count = count + 1
            await ctx.send(split[1])
        else:
          await ctx.send("cant loop for more than 4 times")
      else:
        await ctx.send("go to #spam-chat idiot")

bot.run(os.getenv("BOT_TOKEN"))
client.run(os.getenv("BOT_TOKEN"))