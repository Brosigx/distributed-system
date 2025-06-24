from flask_login import UserMixin
import hashlib

users = []

class User(UserMixin):

    def __init__(self, id, name, email, password, token, dialogues=[], is_admin=False):
        self.id = id
        self.name = name
        self.email = email
        self.password_hash = hashlib.sha256(password.encode()).hexdigest()
        self.dialogues = dialogues
        self.token = token
        self.is_admin = is_admin

    def set_password(self, password):
        self.password = hashlib.sha256(password.encode()).hexdigest()

    def check_password(self, password):
        return self.password == hashlib.sha256(password.encode).hexdigest()

    def get_user(email):
        for user in users:
            if user.email == email:
                return user
            return None

    def is_busy(self):
        for d in self.dialogues:
            if d.status == 'BUSY':
                return True
        return False

    def json_to_user(json):
        dialogues = []
        for d in json['dialogues']:
            dialogues.append(Conversation.json_to_conversation(d))
        return User(json['id'], json['name'], json['email'], json['password'], json['token'], dialogues)
    
    def get_next(self, dialogue_id):
        for d in self.dialogues:
            if d.dialogue_id == dialogue_id:
                return d.next
        return None

    def get_end(self, dialogue_id):
        for d in self.dialogues:
            if d.dialogue_id == dialogue_id:
                return d.end
        return None
    
    def get_conversation(self, dialogue_id):
        for d in self.dialogues:
            if d.dialogue_id == dialogue_id:
                return d
        return None

    def set_status(self, dialogue_id, status):
        for d in self.dialogues:
            if d.dialogue_id == dialogue_id:
                d.status = status
                return d
        return None
    
    def set_location(self,dialogue_id, location):
        for d in self.dialogues:
            if d.dialogue_id == dialogue_id:
                d.location = location
                return d
        return None
    
    def get_status(self, dialogue_id):
        for d in self.dialogues:
            if d.dialogue_id == dialogue_id:
                return d.status
        return None

    def n_dialogues(self):
        return len(self.dialogues)
    
    def n_messages(self):
        n = 0
        for d in self.dialogues:
            n += len(d.dialogue)
        return n

    def n_finished(self):
        n = 0
        for d in self.dialogues:
            if d.status == 'finished':
                n += 1
        return n

    def __repr__(self):
        return '<User {}>'.format(self.email)

class Conversation:

    def __init__(self, dialogue_id, status, next, end, dialogue=[], location=None):
        self.dialogue_id = dialogue_id
        self.status = status
        self.dialogue = dialogue
        self.next = next
        self.end = end
        self.location = location
    
    def json_to_conversation(json):
        dialogue_list = []
        for d in json['dialogue']:
            dialogue_list.append(Dialogue.json_to_dialogue(d))
        return Conversation(dialogue_id=json['dialogue_id'], status=json['status'], next=json['next'], end=json['end'], dialogue=dialogue_list)
    
    def add_dialogue(self, dialogue):
        self.dialogue.append(dialogue)
    
    def to_string(self):
        return (
            'dialogue_id = ' + self.dialogue_id +
            '\nstatus = ' + self.status +
            '\ndialogue = ' + ''.join([d.to_string() + '\n' for d in self.dialogue]) +
            '\nnext = ' + self.next +
            '\nend = ' + self.end
        )

class Dialogue:

    def __init__(self, prompt, answer, timestamp):
        self.prompt = prompt
        self.answer = answer
        self.timestamp = timestamp

    def json_to_dialogue(json):
        return [Dialogue(**json)]