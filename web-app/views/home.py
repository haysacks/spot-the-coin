import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output, State
import base64
import os
import numpy as np
import json
import requests
from server import app
import tensorflow as tf
from keras.preprocessing import image
from forex_python.converter import CurrencyRates

# Initiate class for converting currency
c = CurrencyRates()
# Initiate the TF model
model = tf.keras.models.load_model('model/improved_mobilenet_model.h5')

# Import JSON index to coin label
with open("./model/dict_label.json") as json_file:
    dict_label = json.load(json_file)

# Import coin names
with open('./model/coin_names.json', 'r') as json_file:
    coin_labels = json.load(json_file)

# Import currency details
with open('./model/currency_dict.json', 'r') as json_file:
    currency_dict = json.load(json_file)

def parse_contents(contents, filename):
    """Define formatting for displaying coins to page"""
    return html.Div([
        html.H5(filename, style={'text-align':'center', 'margin-top': '12px'}),
        html.Img(src=contents,
        style={'display': 'block',
            'margin-left': 'auto',
            'margin-right': 'auto',
            'height': '120px'})
    ])

def parse_noncoin(filename):
    """Define formatting for displaying non-image"""
    return html.Div([
        html.H5(filename+" is not an image file.",
        style={'text-align':'center',
                'margin-top': '12px'
            })
    ])

def predict_file(contents, filenames, country):
    """Predict each image foor loop."""
    save_file(contents, filenames)
    try:
        img = image.load_img('./tmp/'+filenames, target_size=(224, 224))
        os.remove('./tmp/'+filenames)
        x = image.img_to_array(img)/255.0
        x = np.expand_dims(x, axis=0)
        classes = model.predict(x, batch_size=32)
        # Get index of largest probability
        predicted_indices = np.argmax(classes, axis=1)
        directories = dict((v, k) for k, v in dict_label.items())
        predicted_dir = [directories.get(k) for k in predicted_indices]
        predicted_labels = [coin_labels.get(str(k)) for k in predicted_dir]
        currency = predicted_labels[0]['name']+" "+predicted_labels[0]['currency']
        currency_id = predicted_labels[0]['currency_id']
        # Split the currency ID into different API (we use two API Services)
        try:
            currency_val = c.convert(predicted_labels[0]['currency_id'], country, predicted_labels[0]['amount'])
        except:
            query = currency_id+"_"+country
            url = 'https://free.currconv.com/api/v7/convert?q='+query+'&compact=ultra&apiKey=da5adc4a7cb74290455a'
            response = requests.get(url)
            currency_val = response.json()[query]
        return parse_contents(contents, currency), currency_val
    except:
        os.remove('./tmp/'+filenames)
        return parse_noncoin(filenames), 0

def save_file(content, name):
    """Saving file to temp folder before feeded into NN model."""
    data = content.encode("utf8").split(b";base64,")[1]
    with open(os.path.join('./tmp/', name), "wb") as fp:
        fp.write(base64.decodebytes(data))

layout = html.Div(
    children=[
        html.Div(
            className="container",
            children=[
                dcc.Location(id='url_login', refresh=True),
                html.Img(
                    src='/assets/coin.jpg',
                    style={'display': 'block',
                        'margin-left': 'auto',
                        'margin-right': 'auto',
                        'margin-bottom': '30px'},
                    className='twelve columns'),
                html.Div('''Upload your image via the button below:''', id='h1'),
                dcc.Upload(
                    id='upload-image',
                    children=html.Div([
                        'Drag and Drop or ',
                        html.A('Select Files')
                        ]),
                    style={
                        'width': '100%',
                        'height': '60px',
                        'lineHeight': '60px',
                        'borderWidth': '1px',
                        'borderStyle': 'dashed',
                        'borderRadius': '5px',
                        'textAlign': 'center'
                    },
                    multiple=True
                ),
                html.Br(),
                html.Div('Select Target Currency:'),
                dcc.Dropdown(
                    id='select-country',
                    options=[{'label': currency_dict[key][1] + " " + currency_dict[key][0],
                            'value': key} for key in currency_dict.keys()],
                    value='IDR'
                ),
                html.Div(id='output-state'),
                html.Div(id='output-currency')
            ]
        )
    ]
)

@app.callback([Output('output-state', 'children'),
                Output('output-currency', 'children')],
              [Input('upload-image', 'contents')],
              [State('upload-image', 'filename'),
              State('select-country', 'value')])
def update_output(list_of_contents, list_of_filenames, country):
    if list_of_contents is not None:
        get_data = [predict_file(contents, filenames, country) for contents, filenames
                                    in zip(list_of_contents, list_of_filenames)]
        children = [html.Hr()] + [data[0] for data in get_data]
        currency_val = [data[1] for data in get_data]
        target_currency = round(sum(currency_val), 2)
        # Total currency in html
        currency_content = [html.Hr(),
            html.Div("Value in "+currency_dict[country][0]+": ", style={'font-size': '20px'}),
            html.H3(str(target_currency)+" "+country, style={'text-align':'center'})
            ]
        return children, currency_content
