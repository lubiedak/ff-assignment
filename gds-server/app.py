import flask, service
from flask import request, jsonify

app = flask.Flask(__name__)
app.config["DEBUG"] = True


@app.route('/', methods=['GET'])
def home():
    return "<h1>GDS API</h1>" \
           "<p>POST /create-session</p>" \
           "<p>DELETE /delete-session?id={string}</p>" \
           "<p>PUT /keep-alive?id={string}</p>" \
           "<p>GET /cost</p>" \
           "<p>GET /ping?id={string}</p>"


@app.route('/create-session', methods=['POST'])
def create_session():
    service.create_session()


@app.route('/delete-session', methods=['DELETE'])
def delete_session():
    id = get_and_validate_id(request.args)
    return id if "Error:" in id else service.delete_session(id)


@app.route('/keep-alive', methods=['PUT'])
def keep_alive():
    id = get_and_validate_id(request.args)
    return id if "Error:" in id else service.keep_alive(id)


@app.route('/ping', methods=['GET'])
def ping():
    id = get_and_validate_id(request.args)
    return id if "Error:" in id else service.ping(id)


@app.route('/cost', methods=['GET'])
def cost():
    return "1"


def get_and_validate_id(args):
    if 'id' in args:
        return args['id']
    else:
        return "Error: No id field provided. Please specify an id."
    return ""


app.run()
