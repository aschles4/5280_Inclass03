package main

import (
	"context"
	"database/sql"
	"github.com/aws/aws-lambda-go/lambda"
	_ "github.com/go-sql-driver/mysql"
	"github.com/kelseyhightower/envconfig"
	"github.com/pkg/errors"
	"github.com/rs/zerolog"
	"github.com/segmentio/ksuid"
	"os"
)

type LoginRequest struct {
	Email string `json:"email"`
	Password string `json:"password"`
}

type LoginResponse struct {
	Token string `json:"token"`
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

func (h *Handler) HandleRequest(ctx context.Context, req LoginRequest) (*LoginResponse, error) {
	//if user is not in database return error message
	row := h.Stmts["find-user-by-email"].QueryRowContext(ctx, req.Email)

	var userID int64
	var pass string
	err := row.Scan(
		&userID,
		&pass,
	)

	if err != nil {
		return nil, errors.Wrap(err, "could not log user in")
	}

	//if user is in database
	if pass != req.Password {
		return nil, errors.Wrap(err, "credentials do not match")
	}

	//create token
	token := ksuid.New().String()

	//insert into user session table
	inRow, err := h.Stmts["insert-user-session"].ExecContext(ctx, userID, token)
	if err != nil {
		return nil, errors.Wrap(err, "failed to insert user session")
	}

	_, err = inRow.RowsAffected()
	if err != nil {
		return nil, errors.Wrap(err, "failed to insert user session")
	}

	_, err = inRow.LastInsertId()
	if err != nil {
		return nil, errors.Wrap(err, "failed to insert user session")
	}

	resp :=  LoginResponse{
		Token: token,
	}

	if err != nil {
		return nil, errors.Wrap(err, "failed to marshal response")
	}

	return &resp, nil
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
		"find-user-by-email": `
			SELECT
				u.id,
				u.password
			FROM users.users u
			WHERE u.email = ?
			LIMIT 1;
		`,
		"insert-user-session": `
			INSERT INTO 
			users.sessions
			(userId, token)
			VALUES (?,?);
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