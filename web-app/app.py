# index page
import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output

from server import app, server
from views import home, nope, instruction

app.title = 'Spot The Coin - Bangk!t JKT2-A'

header = html.Div(
    className='header',
    children=html.Div(
        className='container-width',
        style={'height': '100%'},
        children=[
            html.A([
                html.Img(
                    src='assets/bangkit.png',
                    className='logo'
                )
            ], href="/"),
            html.Div(className='links', children=[
                html.Div(id='instruction', className='link')
            ])
        ]
    )
)

app.layout = html.Div(
    [
        header,
        html.Div([
            html.Div(
                html.Div(id='page-content', className='content'),
                className='content-container'
            ),
        ], className='container-width'),
        dcc.Location(id='url', refresh=False),
    ]
)


@app.callback(Output('page-content', 'children'),
              [Input('url', 'pathname')])
def display_page(pathname):
    if pathname == '/':
        return home.layout
    elif pathname == '/instruction':
        return instruction.layout
    else:
        return nope.layout


@app.callback(
    Output('instruction', 'children'),
    [Input('page-content', 'children')])
def user_logout(input1):
    return html.A('Instruction', href='/instruction', className='button')

if __name__ == '__main__':
    app.run_server(debug=True, host='0.0.0.0')
