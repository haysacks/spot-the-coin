# Dash configuration
import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output

from server import app

# Create app layout
layout = html.Div(children=[
    dcc.Location(id='instruction-url', refresh=True),
    html.Div(
        className="container",
        children=[
            html.Div(
                children=[
                html.H3("Instruction", style={'text-align':'center', 'margin-bottom':'40px'}),
                html.Div("""You can use this web to identify the foreign currency of
                        a coin in images you have. Then, you can choose the
                        targetted currency. The currency value of the coins will
                        be converted automatically into the currency you chose.""",
                        style={'text-align':'justify'}),
                html.Img(
                    src='assets/instruction/1.jpg',
                    style={
                        'display': 'block',
                        'margin-left': 'auto',
                        'margin-right': 'auto',
                        'margin-top': '14px',
                        'margin-bottom': '14px',
                        'border': '1px solid black'
                    },
                    className='twelve columns'
                ),
                html.Div("""You can load multiple images by clicking the button that
                designated with number 1, and please select the targetted currency
                that is designated with number 2 before you proceed. After clicking
                "Select Files" button, a dialog box will open.""",
                style={'text-align':'justify'}),
                html.Img(
                    src='assets/instruction/2.jpg',
                    style={
                        'display': 'block',
                        'margin-left': 'auto',
                        'margin-right': 'auto',
                        'margin-top': '14px',
                        'margin-bottom': '14px',
                        'border': '1px solid black'
                    },
                    className='twelve columns'
                ),
                html.Div("""You will get the result of what currency in each coin
                and the value of all coins in the targetted currency as below.""",
                style={'text-align':'justify'}),
                html.Img(
                    src='assets/instruction/3.jpg',
                    style={
                        'display': 'block',
                        'margin-left': 'auto',
                        'margin-right': 'auto',
                        'margin-top': '14px',
                        'margin-bottom': '40px',
                        'border': '1px solid black'
                    },
                    className='twelve columns'
                ),
                html.Hr(),
                html.H3("About This Project",
                    style={'text-align':'center',
                        'margin-bottom':'20px'}),
                html.Div(children=("More info about this project can be found in",
                html.A('this link.', href='https://github.com/haysacks/spot-the-coin'))),
                html.Div(
                    children=[
                        html.Button(id='instruction-back-button',
                            children='Go to Home',
                            n_clicks=0,
                            style={
                                'margin-top':'50px',
                                'display': 'block',
                                'margin-left': 'auto',
                                'margin-right': 'auto',
                                'background-color':'#a93226',
                                'color':'white'
                            }, className="row")
                    ]
                ),
                ]
            )
        ]
    )
])


# Create callbacks
@app.callback(Output('instruction-url', 'pathname'),
              [Input('instruction-back-button', 'n_clicks')])
def logout_dashboard(n_clicks):
    if n_clicks > 0:
        return '/'
