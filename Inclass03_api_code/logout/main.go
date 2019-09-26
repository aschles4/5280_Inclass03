package main

import (
	"context"
	"database/sql"
	"github.com/aws/aws-lambda-go/lambda"
	_ "github.com/go-sql-driver/mysql"
	"github.com/kelseyhightower/envconfig"
	"github.com/pkg/errors"
	"github.com/rs/zerolog"
	"os"
)

type LogoutRequest struct {
	Token string `json:"token"`
}

type LogoutResponse struct {
}

type Env struct {
	Connection string `required:"true" default:"" envconfig:"CONNECTION"`
}

type Handler struct {
	Env Env
	DB *sql.DB
	Stmts map[string]*sql.Stmt
	l zerolog.Logger
}

func (h *Handler) HandleRequest(ctx context.Context, req LogoutRequest) (*LogoutResponse, error) {
	//check if token is valid and user
	_, err := h.Stmts["delete-user-session"].ExecContext(ctx, req.Token)
	if err != nil {
		return nil, errors.Wrap(err, "could not logout")
	}

	return &LogoutResponse{}, nil
}

func main() {

	l := zerolog.New(os.Stderr).With().Timestamp().Logger()

	var e Env
	err := envconfig.Process("", &e)
	if err != nil {
		l.Fatal().Msg("failed to parse envs")
	}

	db, err := sql.Open("mysql", e.Connection)
	if err != nil {
		l.Fatal().Msg("failed to connect to database")
		panic(err.Error())
	}

	defer db.Close()

	unprepared := map[string]string{
		"delete-user-session": `
			UPDATE users.sessions 
			SET active = 0
			WHERE token = ?;
		`,
	}

	stmts, err := PrepareStmts(db, unprepared)
	if err != nil {
		l.Fatal().Msg("could not prepare mysql statements")
	}

	h := Handler{
		Env: e,
		DB: db,
		Stmts: stmts,
		l: l,
	}


	lambda.Start(h.HandleRequest)
}

func PrepareStmts(db *sql.DB, unprepared map[string]string) (map[string]*sql.Stmt, error) {
	prepared := map[string]*sql.Stmt{}
	for k, v := range unprepared {
		s, err := db.Prepare(v)
		if err != nil {
			return nil, err
		}
		prepared[k] = s
	}
	return prepared, nil
}