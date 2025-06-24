import datetime
import hashlib
import json
import logging
import time
from flask import Flask, render_template, send_from_directory, url_for, request, redirect, flash, session, jsonify
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
import requests
import os

# Usuarios
from models import Conversation, Dialogue, users, User

# Login
from forms import LoginForm, RegisterForm

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión
logging.basicConfig(level=logging.INFO)

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'

@app.route('/static/<path:path>')
def serve_static(path):
    return send_from_directory('static', path)

@app.route('/')
def index():
    if current_user.is_authenticated:
        return redirect(url_for('chat'))
    else:
        return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        session['current_conversation'] = None
        form = LoginForm(None if request.method != 'POST' else request.form)
        if request.method == "POST":
            if form.validate():
                url = 'http://backend-rest:8080/Service/checkLogin'
                headers = {'Content-type': 'application/json'}
                data = {
                    'email': form.email.data,
                    'password': form.password.data
                }
                response = requests.post(url,headers=headers,json=data)
                if response.status_code == 200:
                    user_json = response.json()
                    user = User.json_to_user(user_json)
                    users.append(user)
                    login_user(user, remember=form.remember_me.data)
                    return redirect(url_for('index'))
                else:
                    error = 'Invalid Credentials. Please try again.'
                
            #if form.email.data != 'admin@um.es' or form.password.data != 'admin':
            #    error = 'Invalid Credentials. Please try again.'
            #else:
            #    user = User(1, 'admin', form.email.data.encode('utf-8'),
            #                form.password.data.encode('utf-8'))
            #    users.append(user)
            #    login_user(user, remember=form.remember_me.data)
            #    return redirect(url_for('index'))

        return render_template('login.html', form=form,  error=error)

@app.route('/signup', methods=['GET', 'POST'])
def signup():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = RegisterForm(None if request.method != 'POST' else request.form)
        if request.method == 'POST':
            if form.validate():
                url = 'http://backend-rest:8080/Service/u/signup'
                headers = {'Content-Type': 'application/json'}
                data = {
                    'name': form.username.data,
                    'email': form.email.data,
                    'password': form.password.data
                }
                response = requests.post(url,headers=headers,json=data)
                if response.status_code == 201: # Created
                    user_json = response.json()
                    user = User(user_json['id'], user_json['name'], user_json['email'], user_json['password'], user_json['token'], user_json['dialogues'])
                    users.append(user)
                    flash('User ' + form.email.data + ' registered.', 'success')
                elif response.status_code == 409: # Conflict
                    flash('User ' + form.email.data + ' already registered', 'warning')
            else:
                flash('Error. Please complete all fields and ensure that your password is entered correctly', 'danger')

    return render_template('signup.html', form=form, error=error)
        

@app.route('/profile')
def profile():
    if not current_user.is_authenticated:
        return redirect(url_for('index'))
    return render_template('profile.html', user=current_user)

@app.route('/chat', methods=['GET', 'POST'])
def chat():
    if not current_user.is_authenticated:
        return redirect(url_for('index'))
    error = None
    success = None
    current_conversation = current_user.get_conversation(session.get('current_conversation')) if session.get('current_conversation') else None
    if current_user.is_busy():
        if check_prompts():
            success = 'Answer received'
    if request.method == 'POST':
        action = request.form.get('action')
        
        if action == 'new':
            chat_name = request.form.get('chat_name').strip()
            if not chat_name:
                error='Please, write a chat name'
            elif name_is_used(chat_name):
                error = 'Error, please choose an unused name'
            else:
                is_ok = new_chat_request(chat_name)
                if is_ok:
                    success = 'New chat created: ' + chat_name
                else:
                    error = 'Error creating new chat'
        
        elif action == 'delete':
            chat_to_delete = request.form.get('dialogue_id')
            if not name_is_used(chat_to_delete):
                error = 'Error, please choose an existing name'
            else:
                is_ok  = delete_request(chat_to_delete)
                if is_ok:
                    success = 'Chat deleted: ' + chat_to_delete
                    if current_conversation and current_conversation.dialogue_id == chat_to_delete:
                        session['current_conversation'] = None
                        current_conversation = None
                else:
                    error = 'Error deleting chat'
        
        elif action == 'select':
            selected_chat = request.form.get('dialogue_id')
            for d in current_user.dialogues:
                if d.dialogue_id == selected_chat:
                    session['current_conversation'] = d.dialogue_id
                    current_conversation = d
                    break
            success = 'Selected chat: ' + selected_chat
            redirect(url_for('chat'))
        
        elif action == 'send':
            message_content = request.form.get('message')
            if message_content:
                if current_conversation.status == 'READY':
                    timestamp = int(time.time())
                    if send_prompt(message_content, current_conversation.dialogue_id, timestamp):
                        success = "Message sent. Waiting for the answer..."
                elif current_conversation.status == 'BUSY':
                    error = "Error, please wait for the answer"
                elif current_conversation.status == 'FINISHED':
                    error = "Error, please create a new chat"
        elif action == 'end':
            selected_chat = current_conversation
            logging.info('selected_chat: ' + str(selected_chat.dialogue_id))
            if selected_chat:
                logging.info('Hola he entrado en selected_chat')
                if end_conv(selected_chat.dialogue_id):
                    success = 'Chat ended'
                    session['current_conversation'] = None
                    current_conversation = None
                else:
                    error = 'Error ending chat'
        if current_conversation:
            logging.info('current_conversation dialogues:')
            for d in current_conversation.dialogue:
                logging.info('prompt: ' + d[0].prompt)
                logging.info('answer: ' + d[0].answer)
    return render_template('chat.html', dialogues=current_user.dialogues, current_conversation=current_conversation, error=error, success=success)

@app.route('/logout')
def logout():
    if current_user.is_authenticated:
        logout_user() 
        session.clear()
    return redirect(url_for('index'))

@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == user_id:
            return user
    return None

def is_user(email):
    for user in users:
        if user.email == email:
            return user
    return None

def name_is_used(chat_name):
    for d in current_user.dialogues:
        if d.dialogue_id == chat_name:
            return True
    return False

# Requests to backend 
def new_chat_request(chat_name):
    # Fecha y hora actual en UTC
    now = datetime.datetime.now(datetime.timezone.utc)
    date = now.strftime("%Y-%m-%dT%H:%MZ")
    url = 'http://backend-rest:8080/Service/u/' + current_user.id + '/dialogue'
    auth_token = hashlib.md5((url + date + current_user.token).encode()).hexdigest()
    
    headers = {'Content-Type': 'application/json',
               'User': current_user.id,
               'Date': date,
               'Auth-Token': auth_token
    }
    data = {
        'dialogue_id': chat_name
    }
    response = requests.post(url,headers=headers,json=data)
    if response.status_code == 201: # Created
        conv = Conversation.json_to_conversation(response.json())
        current_user.dialogues.append(conv)
        return True
    else:
        return False
def delete_request(chat_name):
    now = datetime.datetime.now(datetime.timezone.utc)
    date = now.strftime("%Y-%m-%dT%H:%MZ")
    url = 'http://backend-rest:8080/Service/u/' + current_user.id + '/dialogue/' + chat_name + '/delete'
    auth_token = hashlib.md5((url + date + current_user.token).encode()).hexdigest()
    headers = {'User': current_user.id,
               'Date': date,
               'Auth-Token': auth_token
    }
    response = requests.post(url, headers=headers)
    if response.status_code == 204: # OK
        # Eliminar el diálogo del usuario
        for d in current_user.dialogues:
            if d.dialogue_id == chat_name:
                current_user.dialogues.remove(d)
                break
        return True
    else:
        return False

def send_prompt(prompt, chat_name, timestamp):
    if current_user.get_status(chat_name) == 'BUSY':
        return False
    now = datetime.datetime.now(datetime.timezone.utc)
    date = now.strftime("%Y-%m-%dT%H:%MZ")
    url = 'http://backend-rest:8080/Service' + current_user.get_next(chat_name)
    auth_token = hashlib.md5((url + date + current_user.token).encode()).hexdigest()
    headers = {'User': current_user.id,
               'Date': date,
               'Auth-Token': auth_token
    }
    data = {
        'prompt': prompt,
        'timestamp': timestamp
    }
    response = requests.post(url, headers=headers, json=data)
    if response.status_code == 202:
        location = response.headers.get('Location')
        current_user.set_status(chat_name, 'BUSY')
        current_user.set_location(chat_name, location)
        return True
    return False

@app.route('/check_prompts', methods=['GET', 'POST'])
def check_prompts():
    if not current_user.is_authenticated:
        redirect(url_for('index'))
    is_checked = False
    list_dialogues = [] #Guarda la lista de conversaciones para las que ha llegado un mensaje
    for d in current_user.dialogues:
        if d.status == 'BUSY':
            now = datetime.datetime.now(datetime.timezone.utc)
            date = now.strftime("%Y-%m-%dT%H:%MZ")
            url = d.location 
            auth_token = hashlib.md5((url + date + current_user.token).encode()).hexdigest()
            headers = {'User': current_user.id,
                       'Date': date,
                       'Auth-Token': auth_token
            }
            response = requests.get(url, headers=headers)
            if response.status_code == 200:
                response = response.json()
                logging.info('json:')
                logging.info(response['dialogue'])
                d.status = response['status']
                d.next = response['next']
                dial = []
                for di in response['dialogue']:
                    dial.append(Dialogue.json_to_dialogue(di))
                d.dialogue = dial
                logging.info('Conversation!:')
                #logging.info(d.to_string())
                session['current_conversation'] = d.dialogue_id
                list_dialogues.append(d.dialogue_id)
                is_checked = True
                d.location = None
    if is_checked:
        return list_dialogues, 200
    else:
        return '', 400

def end_conv(chat_name):
    now = datetime.datetime.now(datetime.timezone.utc)
    date = now.strftime("%Y-%m-%dT%H:%MZ")
    url = 'http://backend-rest:8080/Service' + current_user.get_end(chat_name)
    auth_token = hashlib.md5((url + date + current_user.token).encode()).hexdigest()
    headers = {'User': current_user.id,
               'Date': date,
               'Auth-Token': auth_token
    }
    logging.info('url: ' + url)
    response = requests.post(url, headers=headers)
    if response.status_code == 204: # OK
        for d in current_user.dialogues:
            if d.dialogue_id == chat_name:
                d.status = 'FINISHED'
                break
        return True
    else:
        return False
            
    

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5010)))
