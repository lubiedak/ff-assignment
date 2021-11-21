import uuid, time, flask


class Service:
    def __init__(self):
        self.cost = 0
        self.sessions = dict()
        self.error_message = "Error: there is no session with given id: "

    def create_session(self):
        time.sleep(1.5)
        id = uuid.uuid4().hex
        self.sessions[id] = time.time()
        self.cost += 100
        return id, 201

    def delete_session(self, id):
        time.sleep(1.5)
        if id in self.sessions:
            del self.sessions[id]
            self.cost += 10
            return "Deleted", 204
        else:
            return self.error_message + id, 404

    def keep_alive(self, id):
        self.cost += 12
        if id in self.sessions:
            self.sessions[id] = time.time()
            return "Session " + id + "refreshed.", 205
        else:
            return self.error_message + id, 404

    def ping(self, id):
        self.cost += 5
        if id in self.sessions:
            return "Session " + id + "exists."
        else:
            return self.error_message + id, 404

    def total_cost(self):
        return str(self.cost)

    def all_sessions(self):
        return flask.jsonify(self.sessions)
